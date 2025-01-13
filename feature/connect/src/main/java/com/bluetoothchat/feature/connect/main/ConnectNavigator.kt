package com.bluetoothchat.feature.connect.main

import com.bluetoothchat.core.ui.navigation.Navigator

interface ConnectNavigator : Navigator {

    fun navigateToCreateGroupScreen()

    fun navigateToPrivateChatScreen(chatId: String)

    fun navigateToGroupChatScreen(chatId: String)

}
