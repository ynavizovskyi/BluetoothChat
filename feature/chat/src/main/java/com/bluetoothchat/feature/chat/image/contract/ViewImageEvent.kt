package com.bluetoothchat.feature.chat.image.contract

import com.bluetoothchat.core.ui.mvi.contract.ViewOneTimeEvent

internal sealed interface ViewImageEvent : ViewOneTimeEvent {

    object NavigateBack : ViewImageEvent

    object ShowImageSavedSnackbar : ViewImageEvent

    object RequestWriteStoragePermission : ViewImageEvent


}
