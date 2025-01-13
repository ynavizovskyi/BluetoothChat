package com.bluetoothchat.feature.settings.ui.contract

import com.bluetoothchat.core.prefs.settings.model.ChatAppTheme
import com.bluetoothchat.core.ui.components.dialog.model.DialogResult
import com.bluetoothchat.core.ui.mvi.contract.ViewAction

internal sealed interface SettingsAction : ViewAction {

    object BackClicked : SettingsAction

    data class ThemeClicked(val theme: ChatAppTheme) : SettingsAction

    data class OnDialogResult(val result: DialogResult) : SettingsAction

    object PrivacyPolicyClicked : SettingsAction

    object TermsOfUseClicked : SettingsAction

    object ContactSupportClicked : SettingsAction

    object OnContactSupportClientResolved : SettingsAction

    object OnContactSupportClientNotFound : SettingsAction

}
