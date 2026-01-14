package com.bluetoothchat.app.navigation

import androidx.navigation.NavController
import com.bluetoothchat.core.analytics.consts.SOURCE_PRIVATE_CHAT
import com.bluetoothchat.core.ui.components.dialog.model.DialogResult
import com.bluetoothchat.core.ui.navigation.BaseNavigator
import com.bluetoothchat.feature.chat.destinations.ViewImageScreenDestination
import com.bluetoothchat.feature.chat.image.ViewImageInputParams
import com.bluetoothchat.feature.chat.privat.PrivateChatNavigator
import com.bluetoothchat.feature.profile.ProfileInputParams
import com.bluetoothchat.feature.profile.ProfileLaunchMode
import com.bluetoothchat.feature.profile.destinations.ProfileScreenDestination
import com.ramcosta.composedestinations.navigation.navigate
import com.ramcosta.composedestinations.result.OpenResultRecipient

class PrivateChatNavigatorImpl(navController: NavController, dialogResultRecipient: OpenResultRecipient<DialogResult>) :
    BaseNavigator(navController, dialogResultRecipient), PrivateChatNavigator {

    override fun navigateToViewImageScreen(chatId: String, messageId: String) {
        navController.navigate(
            direction = ViewImageScreenDestination(
                ViewImageInputParams(
                    chatId = chatId,
                    messageId = messageId,
                    source = SOURCE_PRIVATE_CHAT,
                )
            )
        )
    }

    override fun navigateToUserScreen(userDeviceAddress: String) {
        navController.navigate(
            direction = ProfileScreenDestination(
                ProfileInputParams(
                    mode = ProfileLaunchMode.Other(userDeviceAddress),
                    source = SOURCE_PRIVATE_CHAT,
                )
            )
        )
    }


}
