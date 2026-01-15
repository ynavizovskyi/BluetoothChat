package com.bluetoothchat.app.deeplink

import androidx.navigation.NavController
import androidx.navigation.navOptions
import com.bluetoothchat.core.analytics.consts.SOURCE_NOTIFICATION
import com.bluetoothchat.feature.chat.destinations.GroupChatScreenDestination
import com.bluetoothchat.feature.chat.destinations.PrivateChatScreenDestination
import com.bluetoothchat.feature.chat.group.GroupChatInputParams
import com.bluetoothchat.feature.chat.privat.PrivateChatInputParams
import com.bluetoothchat.feature.connect.destinations.ConnectScreenDestination
import com.bluetoothchat.feature.connect.main.ConnectInputParams
import com.bluetoothchat.feature.main.destinations.MainScreenDestination
import kotlinx.coroutines.flow.first

class DeeplinkNavigatorImpl(
    private val navController: NavController,
) : DeeplinkNavigator {

    override suspend fun navigateToConnectScreen() {
        awaitForNavControllerInit()

        navController.navigate(
            ConnectScreenDestination(
                ConnectInputParams(
                    startScanningOnStart = false,
                    source = SOURCE_NOTIFICATION,
                )
            ).route,
            navOptions = navOptions {
                popUpTo(
                    route = MainScreenDestination.route,
                    popUpToBuilder = { inclusive = false }
                )
            },
        )
    }

    override suspend fun navigateToPrivateChat(chatId: String) {
        awaitForNavControllerInit()

        navController.navigate(
            PrivateChatScreenDestination(
                PrivateChatInputParams(
                    chatId = chatId,
                    source = SOURCE_NOTIFICATION,
                )
            ).route,
            navOptions = navOptions {
                popUpTo(
                    route = MainScreenDestination.route,
                    popUpToBuilder = { inclusive = false }
                )
            },
        )
    }

    override suspend fun navigateToGroupChat(chatId: String) {
        awaitForNavControllerInit()

        navController.navigate(
            GroupChatScreenDestination(
                GroupChatInputParams(
                    chatId = chatId,
                    source = SOURCE_NOTIFICATION,
                )
            ).route,
            navOptions = navOptions {
                popUpTo(
                    route = MainScreenDestination.route,
                    popUpToBuilder = { inclusive = false }
                )
            },
        )
    }

    private suspend fun awaitForNavControllerInit() =
        navController.currentBackStackEntryFlow
            .first()

}
