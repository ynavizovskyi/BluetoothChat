package com.bluetoothchat.feature.main

import com.bluetoothchat.core.ui.navigation.Navigator

interface MainNavigator : Navigator {

    fun navigateToConnectScreen()

    fun navigateToGroupChatScreen(chatId: String)

    fun navigateToPrivateChatScreen(chatId: String)

    fun navigateToCurrentUserProfileScreen()

    fun navigateToSettings()

}
