package com.bluetoothchat.feature.main

import androidx.compose.runtime.compositionLocalOf
import com.bluetoothchat.core.ui.navigation.Navigator

val LocalMainNavigator = compositionLocalOf<MainNavigator> { error("MainNavigator not provided") }

interface MainNavigator : Navigator {

    fun navigateToConnectScreen()

    fun navigateToGroupChatScreen(chatId: String)

    fun navigateToPrivateChatScreen(chatId: String)

    fun navigateToCurrentUserProfileScreen()

    fun navigateToSettings()

}
