package com.bluetoothchat.feature.chat.group

import androidx.compose.runtime.compositionLocalOf
import com.bluetoothchat.core.ui.navigation.Navigator

val LocalGroupChatNavigator = compositionLocalOf<GroupChatNavigator> { error("GroupChatNavigator not provided") }

interface GroupChatNavigator : Navigator {

    fun navigateToChatInfoScreen(chatId: String)

    fun navigateToUserScreen(userDeviceAddress: String)

    fun navigateToViewImageScreen(chatId: String, messageId: String)

}
