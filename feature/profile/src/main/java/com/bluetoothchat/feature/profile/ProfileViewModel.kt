package com.bluetoothchat.feature.profile

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.bluetoothchat.core.bluetooth.message.manager.CommunicationManagerImpl
import com.bluetoothchat.core.db.datasource.UserDataSource
import com.bluetoothchat.core.dispatcher.DispatcherManager
import com.bluetoothchat.core.filemanager.file.FileManager
import com.bluetoothchat.core.filemanager.image.ImageProcessor
import com.bluetoothchat.core.session.Session
import com.bluetoothchat.core.ui.components.dialog.model.DialogInputParams
import com.bluetoothchat.core.ui.components.dialog.model.InvalidImageDialogInputParams
import com.bluetoothchat.core.ui.model.UiText
import com.bluetoothchat.core.ui.model.ViewUser
import com.bluetoothchat.core.ui.model.mapper.ViewUserMapper
import com.bluetoothchat.core.ui.model.toDomain
import com.bluetoothchat.core.ui.model.toPictureDomain
import com.bluetoothchat.core.ui.mvi.MviViewModel
import com.bluetoothchat.feature.profile.contract.EditMode
import com.bluetoothchat.feature.profile.contract.ProfileAction
import com.bluetoothchat.feature.profile.contract.ProfileEvent
import com.bluetoothchat.feature.profile.contract.ProfileState
import com.bluetoothchat.feature.profile.destinations.ProfileScreenDestination
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.bluetoothchat.core.ui.R as CoreUiR

@HiltViewModel
internal class ProfileViewModel @Inject constructor(
    private val session: Session,
    private val userDataSource: UserDataSource,
    private val userMapper: ViewUserMapper,
    private val communicationManager: CommunicationManagerImpl,
    private val imageProcessor: ImageProcessor,
    private val fileManager: FileManager,
    private val analyticsClient: ProfileAnalyticsClient,
    private val dispatcherManager: DispatcherManager,
    savedStateHandle: SavedStateHandle,
) : MviViewModel<ProfileState, ProfileAction, ProfileEvent>(
    ProfileState.Loading
) {

    private val inputParams = ProfileScreenDestination.argsFrom(savedStateHandle)

    init {
        observeState()
    }

    private fun observeState() {
        viewModelScope.launch(dispatcherManager.default) {
            val userFlow = when (val mode = inputParams.mode) {
                is ProfileLaunchMode.Me -> session.observeUser()
                is ProfileLaunchMode.Other -> userDataSource.observe(mode.userDeviceAddress)
            }

            combine(
                userFlow,
                communicationManager.connectedDevicesFlow.onStart { emit(emptyList()) },
            ) { user, connectedDevices ->
                if (user != null) {
                    val viewUser = userMapper.map(user = user, connectedDevices = connectedDevices)

                    val currentState = state.value
                    val updatedState = if (currentState is ProfileState.Loaded) {
                        currentState.copyState(user = viewUser)
                    } else {
                        createInitialState(user = viewUser)
                    }
                    setState { updatedState }
                }
            }
                .collect()
        }
    }

    private suspend fun createInitialState(user: ViewUser): ProfileState.Loaded {
        val isMe = session.isCurrentUser(user.deviceAddress)

        return if (isMe) {
            val launchMode = inputParams.mode
            val isInitialSetUp = (launchMode as? ProfileLaunchMode.Me)?.isInitialSetUp == true

            analyticsClient.reportScreenShownMyProfile(isInitialSetup = isInitialSetUp, source = inputParams.source)

            val editMod = if (isInitialSetUp) {
                EditMode.Editing(
                    updatedName = user.userName,
                    updatedColor = user.color,
                    updatedPictureFileState = null,
                    displayEmptyUpdatedNameError = false,
                )
            } else {
                EditMode.None
            }
            ProfileState.Loaded.Me(user = user, editMode = editMod, isInitialSetUp = isInitialSetUp)
        } else {
            analyticsClient.reportScreenShownOthersProfile(source = inputParams.source)

            ProfileState.Loaded.Other(user = user)
        }
    }

    override fun handleAction(action: ProfileAction) {
        when (action) {
            is ProfileAction.BackButtonClicked -> handleBackButtonClicked()
            is ProfileAction.EditClicked -> handleEditClicked()
            is ProfileAction.SaveClicked -> handleSaveClicked()
            is ProfileAction.ChangePhotoClicked -> handleChangePhotoClicked()
            is ProfileAction.DeletePhotoClicked -> handleDeletePhotoClicked()
            is ProfileAction.ExternalGalleryContentSelected -> handleExternalGalleryPhotoSelected(action)
            is ProfileAction.OnUsernameChanged -> handleUsernameChanged(action)
            is ProfileAction.OnColorChanged -> handleColorChanged(action)
        }
    }

    private fun handleBackButtonClicked() {
        val meState = state.value as? ProfileState.Loaded.Me
        if (meState?.editMode is EditMode.Editing && !meState.isInitialSetUp) {
            setState { meState.copy(editMode = EditMode.None) }
        } else {
            sendEvent { ProfileEvent.NavigateBack }
        }
    }

    private fun handleEditClicked() {
        val meState = state.value as? ProfileState.Loaded.Me ?: return

        viewModelScope.launch(dispatcherManager.default) {
            analyticsClient.reportEditClicked()
            val editMode = EditMode.Editing(
                updatedName = meState.user.userName,
                updatedColor = meState.user.color,
                updatedPictureFileState = meState.user.pictureFileState,
                displayEmptyUpdatedNameError = false,
            )
            setState { meState.copy(editMode = editMode) }
        }
    }

    private fun handleSaveClicked() {
        val meState = state.value as? ProfileState.Loaded.Me ?: return
        val editModeEditing = meState.editMode as? EditMode.Editing ?: return

        viewModelScope.launch(dispatcherManager.default) {
            val launchMode = inputParams.mode
            val isInitialSetUp = (launchMode as? ProfileLaunchMode.Me)?.isInitialSetUp == true

            if (editModeEditing.updatedName.isNullOrEmpty()) {
                analyticsClient.reportSaveError(
                    isInitialSetup = isInitialSetUp,
                    source = inputParams.source,
                    isProfileImageSet = editModeEditing.updatedPictureFileState != null,
                )

                setState { meState.copy(editMode = editModeEditing.copy(displayEmptyUpdatedNameError = true)) }
                sendEvent {
                    ProfileEvent.ShowDialog(
                        DialogInputParams.TextDialog(
                            title = UiText.Resource(CoreUiR.string.dialog_error),
                            message = UiText.Resource(CoreUiR.string.profile_name_error_empty),
                        )
                    )
                }
            } else {
                userDataSource.save(
                    meState.user.toDomain().copy(
                        color = editModeEditing.updatedColor,
                        userName = editModeEditing.updatedName,
                        picture = editModeEditing.updatedPictureFileState?.toPictureDomain(),
                    )
                )

                analyticsClient.reportSaveSuccess(
                    isInitialSetup = isInitialSetUp,
                    source = inputParams.source,
                    isProfileImageSet = editModeEditing.updatedPictureFileState != null,
                )

                if (isInitialSetUp) {
                    sendEvent { ProfileEvent.NavigateToMain }
                } else {
                    setState { meState.copy(editMode = EditMode.None) }
                }
            }
        }
    }

    private fun handleChangePhotoClicked() {
        sendEvent { ProfileEvent.OpenExternalGalleryForImage }
    }

    private fun handleDeletePhotoClicked() {
        val meState = state.value as? ProfileState.Loaded.Me ?: return
        val editModeEditing = meState.editMode as? EditMode.Editing ?: return

        val updatedEditMode = editModeEditing.copy(updatedPictureFileState = null)
        setState { meState.copy(editMode = updatedEditMode) }
    }


    private fun handleExternalGalleryPhotoSelected(action: ProfileAction.ExternalGalleryContentSelected) {
        val meState = state.value as? ProfileState.Loaded.Me ?: return
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
                sendEvent { ProfileEvent.ShowDialog(InvalidImageDialogInputParams) }
            }
        }
    }

    private fun handleUsernameChanged(action: ProfileAction.OnUsernameChanged) {
        val meState = state.value as? ProfileState.Loaded.Me ?: return
        val editModeEditing = meState.editMode as? EditMode.Editing ?: return

        val updatedEditMode =
            editModeEditing.copy(updatedName = action.name, displayEmptyUpdatedNameError = action.name.isEmpty())
        setState { meState.copy(editMode = updatedEditMode) }
    }


    private fun handleColorChanged(action: ProfileAction.OnColorChanged) {
        val meState = state.value as? ProfileState.Loaded.Me ?: return
        val editModeEditing = meState.editMode as? EditMode.Editing ?: return

        val updatedEditMode = editModeEditing.copy(updatedColor = action.color)
        setState { meState.copy(editMode = updatedEditMode) }
    }

}
