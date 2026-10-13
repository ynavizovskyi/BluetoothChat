package com.bluetoothchat.app.navigation

import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.bluetoothchat.core.analytics.consts.SOURCE_CONNECT
import com.bluetoothchat.feature.chat.group.GroupChatInputParams
import com.bluetoothchat.feature.chat.privat.PrivateChatInputParams
import com.bluetoothchat.feature.connect.main.ConnectNavigator

class ConnectNavigatorImpl(backStack: NavBackStack<NavKey>) : BaseNavigator(backStack), ConnectNavigator {

    override fun navigateToCreateGroupScreen() {
        backStack.add(NavRoutes.CreateGroup)
    }

    override fun navigateToPrivateChatScreen(chatId: String) {
        backStack.add(NavRoutes.PrivateChat(PrivateChatInputParams(chatId = chatId, source = SOURCE_CONNECT)))
    }

    override fun navigateToGroupChatScreen(chatId: String) {
        backStack.add(NavRoutes.GroupChat(GroupChatInputParams(chatId = chatId, source = SOURCE_CONNECT)))
    }
}
