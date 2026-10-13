package com.bluetoothchat.feature.connect.main

import androidx.compose.runtime.compositionLocalOf
import com.bluetoothchat.core.ui.navigation.Navigator

val LocalConnectNavigator = compositionLocalOf<ConnectNavigator> { error("ConnectNavigator not provided") }

interface ConnectNavigator : Navigator {

    fun navigateToCreateGroupScreen()

    fun navigateToPrivateChatScreen(chatId: String)

    fun navigateToGroupChatScreen(chatId: String)

}
