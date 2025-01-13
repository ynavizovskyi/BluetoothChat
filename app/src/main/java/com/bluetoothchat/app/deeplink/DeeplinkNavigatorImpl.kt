package com.bluetoothchat.app.deeplink

import androidx.navigation.NavController
import com.bluetoothchat.core.analytics.consts.SOURCE_NOTIFICATION
import com.bluetoothchat.feature.chat.destinations.GroupChatScreenDestination
import com.bluetoothchat.feature.chat.destinations.PrivateChatScreenDestination
import com.bluetoothchat.feature.chat.group.GroupChatInputParams
import com.bluetoothchat.feature.chat.privat.PrivateChatInputParams
import com.bluetoothchat.feature.connect.destinations.ConnectScreenDestination
import com.bluetoothchat.feature.connect.main.ConnectInputParams
import com.bluetoothchat.feature.main.destinations.MainScreenDestination
import com.ramcosta.composedestinations.navigation.navigate
import com.ramcosta.composedestinations.navigation.popBackStack
import kotlinx.coroutines.flow.first

class DeeplinkNavigatorImpl(
    private val navController: NavController,
) : DeeplinkNavigator {

    override suspend fun navigateToConnectScreen() {
        awaitForNavControllerInit()

        navController.popBackStack(route = MainScreenDestination, inclusive = false)
        navController.navigate(
            ConnectScreenDestination(
                ConnectInputParams(
                    startScanningOnStart = false,
                    source = SOURCE_NOTIFICATION,
                )
            )
        )
    }

    override suspend fun navigateToPrivateChat(chatId: String) {
        awaitForNavControllerInit()

        navController.popBackStack(route = MainScreenDestination, inclusive = false)
        navController.navigate(
            PrivateChatScreenDestination(
                PrivateChatInputParams(
                    chatId = chatId,
                    source = SOURCE_NOTIFICATION,
                )
            )
        )
    }

    override suspend fun navigateToGroupChat(chatId: String) {
        awaitForNavControllerInit()

        navController.popBackStack(route = MainScreenDestination, inclusive = false)
        navController.navigate(
            GroupChatScreenDestination(
                GroupChatInputParams(
                    chatId = chatId,
                    source = SOURCE_NOTIFICATION,
                )
            )
        )
    }

    private suspend fun awaitForNavControllerInit() =
        navController.currentBackStackEntryFlow
            .first()

}
