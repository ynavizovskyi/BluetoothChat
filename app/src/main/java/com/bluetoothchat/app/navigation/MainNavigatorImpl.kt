package com.bluetoothchat.app.navigation

import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.bluetoothchat.core.analytics.consts.SOURCE_MAIN
import com.bluetoothchat.feature.chat.group.GroupChatInputParams
import com.bluetoothchat.feature.chat.privat.PrivateChatInputParams
import com.bluetoothchat.feature.connect.main.ConnectInputParams
import com.bluetoothchat.feature.main.MainNavigator
import com.bluetoothchat.feature.profile.ProfileInputParams
import com.bluetoothchat.feature.profile.ProfileLaunchMode

class MainNavigatorImpl(backStack: NavBackStack<NavKey>) : BaseNavigator(backStack), MainNavigator {

    override fun navigateToConnectScreen() {
        backStack.add(
            NavRoutes.Connect(
                ConnectInputParams(
                    startScanningOnStart = true,
                    source = SOURCE_MAIN,
                )
            )
        )
    }

    override fun navigateToGroupChatScreen(chatId: String) {
        backStack.add(NavRoutes.GroupChat(GroupChatInputParams(chatId = chatId, source = SOURCE_MAIN)))
    }

    override fun navigateToPrivateChatScreen(chatId: String) {
        backStack.add(
            NavRoutes.PrivateChat(
                PrivateChatInputParams(
                    chatId = chatId,
                    source = SOURCE_MAIN,
                )
            )
        )
    }

    override fun navigateToCurrentUserProfileScreen() {
        backStack.add(
            NavRoutes.Profile(
                ProfileInputParams(
                    mode = ProfileLaunchMode.Me(isInitialSetUp = false),
                    source = SOURCE_MAIN,
                )
            )
        )
    }

    override fun navigateToSettings() {
        backStack.add(NavRoutes.Settings)
    }
}
