package com.bluetoothchat.feature.connect.group.create

import androidx.lifecycle.viewModelScope
import com.bluetoothchat.core.bluetooth.message.manager.CommunicationManagerImpl
import com.bluetoothchat.core.dispatcher.DispatcherManager
import com.bluetoothchat.core.filemanager.file.FileManager
import com.bluetoothchat.core.filemanager.image.ImageProcessor
import com.bluetoothchat.core.session.Session
import com.bluetoothchat.core.ui.components.dialog.model.InvalidImageDialogInputParams
import com.bluetoothchat.core.ui.model.toPictureDomain
import com.bluetoothchat.core.ui.mvi.MviViewModel
import com.bluetoothchat.feature.connect.group.create.contract.CreateGroupAction
import com.bluetoothchat.feature.connect.group.create.contract.CreateGroupEvent
import com.bluetoothchat.feature.connect.group.create.contract.CreateGroupState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class CreateGroupViewModel @Inject constructor(
    private val session: Session,
    private val communicationManager: CommunicationManagerImpl,
    private val dispatcherManager: DispatcherManager,
    private val fileManager: FileManager,
    private val imageProcessor: ImageProcessor,
    private val analyticsClient: CreateGroupAnalyticsClient,
) : MviViewModel<CreateGroupState, CreateGroupAction, CreateGroupEvent>(
    CreateGroupState(groupImageChatState = null, chatName = "", displayNameError = false)
) {

    init {
        viewModelScope.launch(dispatcherManager.default) {
            analyticsClient.reportScreenShown()
        }
    }

    override fun handleAction(action: CreateGroupAction) {
        when (action) {
            is CreateGroupAction.BackButtonClicked -> handleBackButtonClicked(action)
            is CreateGroupAction.SaveButtonClicked -> handleCreateButtonClicked(action)
            is CreateGroupAction.ExternalGalleryContentSelected -> handleExternalGalleryPhotoSelected(action)
            is CreateGroupAction.ChangePhotoClicked -> handleChangePhotoClicked(action)
            is CreateGroupAction.DeletePhotoClicked -> handleDeletePhotoClicked(action)
            is CreateGroupAction.OnGroupNameChanged -> handleGroupNameChanged(action)
        }
    }

    private fun handleBackButtonClicked(action: CreateGroupAction.BackButtonClicked) {
        sendEvent { CreateGroupEvent.NavigateBack }
    }

    private fun handleCreateButtonClicked(action: CreateGroupAction.SaveButtonClicked) {
        viewModelScope.launch(dispatcherManager.default) {
            if (state.value.chatName.isNotEmpty()) {
                val groupChatId = communicationManager.createGroupChat(
                    name = state.value.chatName,
                    picture = state.value.groupImageChatState?.toPictureDomain(),
                )

                analyticsClient.reportSaveSuccess(isProfileImageSet = state.value.groupImageChatState != null)
                sendEvent { CreateGroupEvent.NavigateToGroupInfo(groupId = groupChatId) }
            } else {
                analyticsClient.reportSaveError(isProfileImageSet = state.value.groupImageChatState != null)
                setState { copy(displayNameError = true) }
            }
        }
    }

    private fun handleChangePhotoClicked(action: CreateGroupAction.ChangePhotoClicked) {
        sendEvent { CreateGroupEvent.OpenExternalGalleryForImage }
    }

    private fun handleDeletePhotoClicked(action: CreateGroupAction.DeletePhotoClicked) {
        setState { copy(groupImageChatState = null) }
    }

    private fun handleGroupNameChanged(action: CreateGroupAction.OnGroupNameChanged) {
        setState { copy(chatName = action.name, displayNameError = action.name.isEmpty()) }
    }

    private fun handleExternalGalleryPhotoSelected(action: CreateGroupAction.ExternalGalleryContentSelected) {
        viewModelScope.launch(dispatcherManager.default) {
            val picture = imageProcessor.saveUserImage(uri = action.uri).getOrNull()
            if (picture != null) {
                val updatedGroupImage =
                    fileManager.getChatAvatarPictureFile(fileName = picture.id, sizeBytes = picture.sizeBytes)

                setState { copy(groupImageChatState = updatedGroupImage) }
            } else {
                sendEvent { CreateGroupEvent.ShowErrorDialog(InvalidImageDialogInputParams) }
            }
        }
    }

}
