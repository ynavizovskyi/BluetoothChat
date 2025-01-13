package com.bluetoothchat.app.navigation

import androidx.navigation.NavController
import com.bluetoothchat.core.ui.components.dialog.model.DialogResult
import com.bluetoothchat.core.ui.navigation.BaseNavigator
import com.bluetoothchat.feature.chat.image.ViewImageNavigator
import com.ramcosta.composedestinations.result.OpenResultRecipient

class ViewImageNavigatorImpl(navController: NavController, dialogResultRecipient: OpenResultRecipient<DialogResult>) :
    BaseNavigator(navController, dialogResultRecipient), ViewImageNavigator {

}
