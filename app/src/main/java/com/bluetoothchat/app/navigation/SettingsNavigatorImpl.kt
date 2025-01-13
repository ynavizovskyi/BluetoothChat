package com.bluetoothchat.app.navigation

import androidx.navigation.NavController
import com.bluetoothchat.core.ui.components.dialog.model.DialogResult
import com.bluetoothchat.core.ui.navigation.BaseNavigator
import com.bluetoothchat.feature.settings.SettingsNavigator
import com.ramcosta.composedestinations.result.OpenResultRecipient

class SettingsNavigatorImpl(navController: NavController, dialogResultRecipient: OpenResultRecipient<DialogResult>) :
    BaseNavigator(navController, dialogResultRecipient), SettingsNavigator {

}

