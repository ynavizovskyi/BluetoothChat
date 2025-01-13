package com.bluetoothchat.feature.chat.image.contract

import com.bluetoothchat.core.filemanager.file.FileState
import com.bluetoothchat.core.ui.model.ViewMessageContent
import com.bluetoothchat.core.ui.mvi.contract.ViewState

internal sealed interface ViewImageState : ViewState {

    data class Loaded(val image: ViewMessageContent.Image) : ViewImageState

    object Loading : ViewImageState

}
