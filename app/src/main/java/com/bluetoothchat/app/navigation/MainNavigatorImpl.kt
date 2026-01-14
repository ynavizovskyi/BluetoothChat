package com.bluetoothchat.app.navigation

import androidx.navigation.NavController
import com.bluetoothchat.core.analytics.consts.SOURCE_MAIN
import com.bluetoothchat.core.ui.components.dialog.model.DialogResult
import com.bluetoothchat.core.ui.navigation.BaseNavigator
import com.bluetoothchat.feature.chat.destinations.GroupChatScreenDestination
import com.bluetoothchat.feature.chat.destinations.PrivateChatScreenDestination
import com.bluetoothchat.feature.chat.group.GroupChatInputParams
import com.bluetoothchat.feature.chat.privat.PrivateChatInputParams
import com.bluetoothchat.feature.connect.destinations.ConnectScreenDestination
import com.bluetoothchat.feature.connect.main.ConnectInputParams
import com.bluetoothchat.feature.main.MainNavigator
import com.bluetoothchat.feature.profile.ProfileInputParams
import com.bluetoothchat.feature.profile.ProfileLaunchMode
import com.bluetoothchat.feature.profile.destinations.ProfileScreenDestination
import com.bluetoothchat.feature.settings.ui.destinations.SettingsScreenDestination
import com.ramcosta.composedestinations.navigation.navigate
import com.ramcosta.composedestinations.result.OpenResultRecipient

class MainNavigatorImpl(navController: NavController, dialogResultRecipient: OpenResultRecipient<DialogResult>) :
    BaseNavigator(navController, dialogResultRecipient), MainNavigator {

    override fun navigateToConnectScreen() {
        navController.navigate(
            direction = ConnectScreenDestination(
                ConnectInputParams(
                    startScanningOnStart = true,
                    source = SOURCE_MAIN,
                )
            )
        )
    }

    override fun navigateToGroupChatScreen(chatId: String) {
        navController.navigate(direction = GroupChatScreenDestination(GroupChatInputParams(chatId = chatId, source = SOURCE_MAIN)))
    }

    override fun navigateToPrivateChatScreen(chatId: String) {
        navController.navigate(
            direction = PrivateChatScreenDestination(
                PrivateChatInputParams(
                    chatId = chatId,
                    source = SOURCE_MAIN,
                )
            )
        )
    }

    override fun navigateToCurrentUserProfileScreen() {
        navController.navigate(
            direction = ProfileScreenDestination(
                ProfileInputParams(
                    mode = ProfileLaunchMode.Me(isInitialSetUp = false),
                    source = SOURCE_MAIN,
                )
            )
        )
    }

    override fun navigateToSettings() {
        navController.navigate(direction = SettingsScreenDestination())
    }
}

