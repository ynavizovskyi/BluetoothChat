package com.bluetoothchat.app.deeplink

import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.bluetoothchat.app.navigation.NavRoutes
import com.bluetoothchat.app.navigation.clearUpTo
import com.bluetoothchat.core.analytics.consts.SOURCE_NOTIFICATION
import com.bluetoothchat.feature.chat.group.GroupChatInputParams
import com.bluetoothchat.feature.chat.privat.PrivateChatInputParams
import com.bluetoothchat.feature.connect.main.ConnectInputParams

class DeeplinkNavigatorImpl(
    private val backStack: NavBackStack<NavKey>,
) : DeeplinkNavigator {

    override fun navigateToConnectScreen() {
        backStack.add(
            NavRoutes.Connect(
                ConnectInputParams(
                    startScanningOnStart = false,
                    source = SOURCE_NOTIFICATION,
                )
            )
        )
        backStack.clearUpTo<NavRoutes.Main>()
    }

    override fun navigateToPrivateChat(chatId: String) {
        backStack.add(
            NavRoutes.PrivateChat(
                PrivateChatInputParams(
                    chatId = chatId,
                    source = SOURCE_NOTIFICATION,
                )
            )
        )
        backStack.clearUpTo<NavRoutes.Main>()
    }

    override fun navigateToGroupChat(chatId: String) {
        backStack.add(
            NavRoutes.GroupChat(
                GroupChatInputParams(
                    chatId = chatId,
                    source = SOURCE_NOTIFICATION,
                )
            )
        )
        backStack.clearUpTo<NavRoutes.Main>()
    }

}
