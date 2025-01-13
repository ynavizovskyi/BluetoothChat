package com.bluetoothchat.app.navigation

import androidx.navigation.NavController
import com.bluetoothchat.core.analytics.consts.SOURCE_GROUP_CHAT
import com.bluetoothchat.core.ui.components.dialog.model.DialogResult
import com.bluetoothchat.core.ui.navigation.BaseNavigator
import com.bluetoothchat.feature.chat.destinations.GroupChatInfoScreenDestination
import com.bluetoothchat.feature.chat.destinations.ViewImageScreenDestination
import com.bluetoothchat.feature.chat.group.GroupChatNavigator
import com.bluetoothchat.feature.chat.group.chatinfo.GroupChatInfoInputParams
import com.bluetoothchat.feature.chat.image.ViewImageInputParams
import com.bluetoothchat.feature.profile.ProfileInputParams
import com.bluetoothchat.feature.profile.ProfileLaunchMode
import com.bluetoothchat.feature.profile.destinations.ProfileScreenDestination
import com.ramcosta.composedestinations.navigation.navigate
import com.ramcosta.composedestinations.result.OpenResultRecipient

class GroupChatNavigatorImpl(navController: NavController, dialogResultRecipient: OpenResultRecipient<DialogResult>) :
    BaseNavigator(navController, dialogResultRecipient), GroupChatNavigator {

    override fun navigateToChatInfoScreen(chatId: String) {
        navController.navigate(GroupChatInfoScreenDestination(GroupChatInfoInputParams(chatId = chatId)))
    }

    override fun navigateToUserScreen(userDeviceAddress: String) {
        navController.navigate(
            ProfileScreenDestination(
                ProfileInputParams(
                    mode = ProfileLaunchMode.Other(
                        userDeviceAddress
                    ),
                    source = SOURCE_GROUP_CHAT,
                )
            )
        )
    }

    override fun navigateToViewImageScreen(chatId: String, messageId: String) {
        navController.navigate(
            ViewImageScreenDestination(
                ViewImageInputParams(
                    chatId = chatId,
                    messageId = messageId,
                    source = SOURCE_GROUP_CHAT,
                )
            )
        )
    }
}
