package com.bluetoothchat.feature.profile.contract

import com.bluetoothchat.core.ui.components.dialog.model.DialogInputParams
import com.bluetoothchat.core.ui.mvi.contract.ViewOneTimeEvent

internal interface ProfileEvent : ViewOneTimeEvent {

    object NavigateBack : ProfileEvent

    object NavigateToMain : ProfileEvent

    object OpenExternalGalleryForImage : ProfileEvent

    data class ShowDialog(val params: DialogInputParams) : ProfileEvent

}
