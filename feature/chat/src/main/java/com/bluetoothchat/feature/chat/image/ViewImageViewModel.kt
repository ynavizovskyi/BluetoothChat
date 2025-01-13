package com.bluetoothchat.feature.chat.image

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.bluetoothchat.core.db.datasource.MessageDataSource
import com.bluetoothchat.core.dispatcher.DispatcherManager
import com.bluetoothchat.core.domain.model.Message
import com.bluetoothchat.core.filemanager.file.FileState
import com.bluetoothchat.core.ui.model.ViewMessageContent
import com.bluetoothchat.core.ui.model.mapper.ViewMessageContentMapper
import com.bluetoothchat.core.ui.mvi.MviViewModel
import com.bluetoothchat.feature.chat.destinations.ViewImageScreenDestination
import com.bluetoothchat.feature.chat.image.contract.ViewImageAction
import com.bluetoothchat.feature.chat.image.contract.ViewImageEvent
import com.bluetoothchat.feature.chat.image.contract.ViewImageState
import com.bluetoothchat.feature.chat.image.saver.ImageSaver
import com.bluetoothchat.feature.chat.image.saver.SaveImageResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class ViewImageViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val messageDataSource: MessageDataSource,
    private val dispatcherManager: DispatcherManager,
    private val messageContentMapper: ViewMessageContentMapper,
    private val imageSaver: ImageSaver,
    private val analyticsClient: ViewImageAnalyticsClient,
) : MviViewModel<ViewImageState, ViewImageAction, ViewImageEvent>(
    ViewImageState.Loading
) {

    private val inputParams = getInputParams(savedStateHandle)

    init {
        viewModelScope.launch(dispatcherManager.default) {
            analyticsClient.reportScreenShown(source = inputParams.source)
        }
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch(dispatcherManager.default) {
            val message = messageDataSource.get(messageId = inputParams.messageId) as? Message.Plain
            val viewImage = message?.content?.firstOrNull()?.let {
                messageContentMapper.map(content = it, chatId = inputParams.chatId, filesBeingDownloaded = emptyList())
            } as? ViewMessageContent.Image
            val imageFile = viewImage?.file as? FileState.Downloaded

            if (imageFile != null) {
                setState { ViewImageState.Loaded(image = viewImage) }
            } else {

            }
        }
    }

    override fun handleAction(action: ViewImageAction) {
        when (action) {
            is ViewImageAction.BackButtonClicked -> handleBackButtonClicked(action)
            is ViewImageAction.SaveClicked -> handleSaveClicked(action)
            is ViewImageAction.OnWriteStoragePermissionResult -> handleWriteStoragePermissionResult(action)
        }
    }

    private fun handleBackButtonClicked(action: ViewImageAction.BackButtonClicked) {
        sendEvent { ViewImageEvent.NavigateBack }
    }

    private fun handleSaveClicked(action: ViewImageAction.SaveClicked) {
        val loadedState = state.value as? ViewImageState.Loaded ?: return
        val downloadedFile = loadedState.image.file as? FileState.Downloaded ?: return

        viewModelScope.launch(dispatcherManager.default) {
            val saveResult = imageSaver.save(filePath = downloadedFile.path)
            when (saveResult) {
                is SaveImageResult.Success -> {
                    sendEvent { ViewImageEvent.ShowImageSavedSnackbar }
                }

                is SaveImageResult.Error -> {
                    if (saveResult is SaveImageResult.Error.NoWriteStoragePermission) {
                        sendEvent { ViewImageEvent.RequestWriteStoragePermission }
                    }
                }
            }
        }
    }

    private fun handleWriteStoragePermissionResult(action: ViewImageAction.OnWriteStoragePermissionResult) {
        val loadedState = state.value as? ViewImageState.Loaded ?: return
        val downloadedFile = loadedState.image.file as? FileState.Downloaded ?: return

        viewModelScope.launch(dispatcherManager.default) {
            if (action.granted) {
                val saveResult = imageSaver.save(filePath = downloadedFile.path)
                if (saveResult is SaveImageResult.Success) {
                    sendEvent { ViewImageEvent.ShowImageSavedSnackbar }
                }
            }
        }
    }

    private companion object {
        fun getInputParams(savedStateHandle: SavedStateHandle) =
            requireNotNull(ViewImageScreenDestination.argsFrom(savedStateHandle))
    }
}
