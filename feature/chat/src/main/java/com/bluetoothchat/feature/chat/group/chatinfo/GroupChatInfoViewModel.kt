package com.bluetoothchat.feature.chat.group.chatinfo

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.bluetoothchat.core.bluetooth.message.manager.CommunicationManagerImpl
import com.bluetoothchat.core.db.datasource.ChatDataSource
import com.bluetoothchat.core.dispatcher.DispatcherManager
import com.bluetoothchat.core.domain.model.GroupUpdateType
import com.bluetoothchat.core.domain.model.User
import com.bluetoothchat.core.filemanager.file.FileManager
import com.bluetoothchat.core.filemanager.image.ImageProcessor
import com.bluetoothchat.core.session.Session
import com.bluetoothchat.core.ui.components.dialog.model.InvalidImageDialogInputParams
import com.bluetoothchat.core.ui.model.ViewChat
import com.bluetoothchat.core.ui.model.ViewUserAction
import com.bluetoothchat.core.ui.model.mapper.ViewUserActionsMapper
import com.bluetoothchat.core.ui.model.mapper.chat.ViewGroupChatMapper
import com.bluetoothchat.core.ui.model.toDomain
import com.bluetoothchat.core.ui.model.toPictureDomain
import com.bluetoothchat.core.ui.mvi.MviViewModel
import com.bluetoothchat.feature.chat.destinations.GroupChatInfoScreenDestination
import com.bluetoothchat.feature.chat.group.chatinfo.contract.EditMode
import com.bluetoothchat.feature.chat.group.chatinfo.contract.GroupChatInfoAction
import com.bluetoothchat.feature.chat.group.chatinfo.contract.GroupChatInfoEvent
import com.bluetoothchat.feature.chat.group.chatinfo.contract.GroupChatInfoState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class GroupChatInfoViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val session: Session,
    private val chatDataSource: ChatDataSource,
    private val communicationManager: CommunicationManagerImpl,
    private val groupChatMapper: ViewGroupChatMapper,
    private val dispatcherManager: DispatcherManager,
    private val imageProcessor: ImageProcessor,
    private val fileManager: FileManager,
    private val analyticsClient: GroupChatInfoAnalyticsClient,
) : MviViewModel<GroupChatInfoState, GroupChatInfoAction, GroupChatInfoEvent>(
    GroupChatInfoState.Loading
) {

    private val inputParams = getInputParams(savedStateHandle)

    //Beware not too call it too ofter, it is expensive
    private val userActionsMapper = object : ViewUserActionsMapper {
        override suspend fun map(user: User, isConnected: Boolean): List<ViewUserAction> {
            val chat = chatDataSource.getGroupChatById(chatId = inputParams.chatId)
            val isUserHost = chat?.let { session.isCurrentUser(chat.hostDeviceAddress) } == true

            //Admin user can remove anyone except themselves
            return if (isUserHost && !session.isCurrentUser(user)) {
                if (isConnected) {
                    listOf(ViewUserAction.REMOVE_FROM_GROUP_AND_DISCONNECT)
                } else {
                    listOf(ViewUserAction.REMOVE_FROM_GROUP)
                }
            } else {
                emptyList()
            }
        }
    }

    init {
        viewModelScope.launch(dispatcherManager.default) {
            val chat = chatDataSource.getGroupChatById(inputParams.chatId)
            if (chat != null) {
                analyticsClient.reportScreenShown(chat = chat)
            }
        }
        observeChatState()
    }

    private fun observeChatState() {
        combine(
            chatDataSource.observeGroupChatById(inputParams.chatId),
            communicationManager.connectedDevicesFlow.onStart { emit(emptyList()) },
            communicationManager.observeUserFilesBeingDownloaded().onStart { emit(emptyList()) },
        ) { chat, connectedDevices, _ ->
            if (chat != null) {
                //Only show direct connections in the group chat if user is the host
                //otherwise they are irrelevant (user might be connected to you but not to chat's host)
                val connectedDevices = if (session.isCurrentUser(chat.hostDeviceAddress)) {
                    connectedDevices
                } else {
                    emptyList()
                }

                val viewChat = groupChatMapper.map(
                    chat = chat,
                    connectedDevices = connectedDevices,
                    userActionsMapper = userActionsMapper,
                )

                val currentState = state.value
                val updatedState = if (currentState is GroupChatInfoState.Loaded) {
                    currentState.copyState(chat = viewChat)
                } else {
                    createInitialState(chat = viewChat)
                }
                setState { updatedState }
            } else {
                //Close screen?
            }
        }
            .flowOn(dispatcherManager.io)
            .launchIn(viewModelScope)
    }

    private suspend fun createInitialState(chat: ViewChat.Group): GroupChatInfoState.Loaded {
        val isUserAdmin = session.isCurrentUser(chat.hostDeviceAddress)

        return if (isUserAdmin) {
            GroupChatInfoState.Loaded.Host(chat = chat, editMode = EditMode.None)
        } else {
            GroupChatInfoState.Loaded.Client(chat = chat)
        }
    }

    override fun handleAction(action: GroupChatInfoAction) {
        when (action) {
            is GroupChatInfoAction.BackButtonClicked -> handleBackButtonClicked(action)
            is GroupChatInfoAction.AddMembersClicked -> handleAddUsersClicked(action)
            is GroupChatInfoAction.UserActionClicked -> handleUserActionClicked(action)
            is GroupChatInfoAction.UserClicked -> handleUserClicked(action)
            is GroupChatInfoAction.EditClicked -> handleEditClicked()
            is GroupChatInfoAction.SaveClicked -> handleSaveClicked()
            is GroupChatInfoAction.ChangePhotoClicked -> handleChangePhotoClicked()
            is GroupChatInfoAction.DeletePhotoClicked -> handleDeletePhotoClicked()
            is GroupChatInfoAction.ExternalGalleryContentSelected -> handleExternalGalleryPhotoSelected(action)
            is GroupChatInfoAction.OnUsernameChanged -> handleUsernameChanged(action)
        }
    }

    private fun handleBackButtonClicked(action: GroupChatInfoAction.BackButtonClicked) {
        val hostState = state.value as? GroupChatInfoState.Loaded.Host
        if (hostState?.editMode is EditMode.Editing) {
            setState { hostState.copy(editMode = EditMode.None) }
        } else {
            sendEvent { GroupChatInfoEvent.NavigateBack }
        }
    }

    private fun handleAddUsersClicked(action: GroupChatInfoAction.AddMembersClicked) {
        sendEvent { GroupChatInfoEvent.NavigateToAddUsersScreen(chatId = inputParams.chatId) }
    }

    private fun handleUserActionClicked(action: GroupChatInfoAction.UserActionClicked) {
        viewModelScope.launch {
            when (action.action) {
                ViewUserAction.REMOVE_FROM_GROUP -> {
                    communicationManager.removeUserFromChat(
                        chatId = inputParams.chatId,
                        updateType = GroupUpdateType.USER_REMOVED,
                        userAddress = action.user.deviceAddress,
                    )
                }

                ViewUserAction.REMOVE_FROM_GROUP_AND_DISCONNECT -> {
                    communicationManager.removeUserFromChat(
                        chatId = inputParams.chatId,
                        updateType = GroupUpdateType.USER_REMOVED,
                        userAddress = action.user.deviceAddress,
                    )

                    //Making sure that the remove message has a decent chance of being sent before disconnecting
                    delay(300)
                    communicationManager.disconnect(deviceAddress = action.user.deviceAddress)
                }
            }
        }
    }

    private fun handleUserClicked(action: GroupChatInfoAction.UserClicked) {
        sendEvent { GroupChatInfoEvent.NavigateToProfileScreen(userDeviceAddress = action.user.deviceAddress) }
    }

    private fun handleEditClicked() {
        val hostState = state.value as? GroupChatInfoState.Loaded.Host ?: return

        viewModelScope.launch(dispatcherManager.default) {
            analyticsClient.reportEditClicked()

            val editMode = EditMode.Editing(
                updatedName = hostState.chat.name,
                updatedPictureFileState = hostState.chat.pictureFileState,
                displayEmptyUpdatedNameError = false,
            )
            setState { hostState.copy(editMode = editMode) }
        }
    }

    private fun handleSaveClicked() {
        val hostState = state.value as? GroupChatInfoState.Loaded.Host ?: return
        val editModeEditing = hostState.editMode as? EditMode.Editing ?: return

        viewModelScope.launch(dispatcherManager.default) {
            if (!editModeEditing.updatedName.isNullOrEmpty()) {
                chatDataSource.save(
                    hostState.chat.toDomain().copy(
                        name = editModeEditing.updatedName,
                        picture = editModeEditing.updatedPictureFileState?.toPictureDomain(),
                    )
                )

                analyticsClient.reportSaveSuccess(isGroupImageSet = editModeEditing.updatedPictureFileState != null)
                setState { hostState.copy(editMode = EditMode.None) }
            } else {
                analyticsClient.reportSaveError(isGroupImageSet = editModeEditing.updatedPictureFileState != null)

                val updatedEditMode = editModeEditing.copy(displayEmptyUpdatedNameError = true)
                setState { hostState.copy(editMode = updatedEditMode) }
            }
        }
    }

    private fun handleChangePhotoClicked() {
        sendEvent { GroupChatInfoEvent.OpenExternalGalleryForImage }
    }

    private fun handleDeletePhotoClicked() {
        val hostState = state.value as? GroupChatInfoState.Loaded.Host ?: return
        val editModeEditing = hostState.editMode as? EditMode.Editing ?: return

        val updatedEditMode = editModeEditing.copy(updatedPictureFileState = null)
        setState { hostState.copy(editMode = updatedEditMode) }
    }


    private fun handleExternalGalleryPhotoSelected(action: GroupChatInfoAction.ExternalGalleryContentSelected) {
        val meState = state.value as? GroupChatInfoState.Loaded.Host ?: return
        val editModeEditing = meState.editMode as? EditMode.Editing ?: return

        viewModelScope.launch(dispatcherManager.default) {
            val picture = imageProcessor.saveUserImage(uri = action.uri).getOrNull()

            if (picture != null) {
                val updatedEditMode = editModeEditing.copy(
                    updatedPictureFileState = fileManager.getChatAvatarPictureFile(
                        fileName = picture.id,
                        sizeBytes = picture.sizeBytes,
                    ),
                )
                setState { meState.copy(editMode = updatedEditMode) }
            } else {
                sendEvent { GroupChatInfoEvent.ShowDialog(InvalidImageDialogInputParams) }
            }
        }
    }

    private fun handleUsernameChanged(action: GroupChatInfoAction.OnUsernameChanged) {
        val hostState = state.value as? GroupChatInfoState.Loaded.Host ?: return
        val editModeEditing = hostState.editMode as? EditMode.Editing ?: return

        val updatedEditMode =
            editModeEditing.copy(updatedName = action.name, displayEmptyUpdatedNameError = action.name.isEmpty())
        setState { hostState.copy(editMode = updatedEditMode) }
    }


    private companion object {
        fun getInputParams(savedStateHandle: SavedStateHandle) =
            requireNotNull(GroupChatInfoScreenDestination.argsFrom(savedStateHandle))
    }
}
