package com.bluetoothchat.feature.settings.ui.contract

import com.bluetoothchat.core.ui.components.dialog.model.DialogInputParams
import com.bluetoothchat.core.ui.mvi.contract.ViewOneTimeEvent

internal sealed interface SettingsEvent : ViewOneTimeEvent {

    object NavigateBack : SettingsEvent

    data class ShowDialog(val params: DialogInputParams) : SettingsEvent

    data class OpenUrl(val url: String) : SettingsEvent

    data class OpenEmailClient(val emailAddress: String, val emailSubject: String) : SettingsEvent

}
