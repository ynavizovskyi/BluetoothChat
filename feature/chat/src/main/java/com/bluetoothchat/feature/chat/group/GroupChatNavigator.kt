package com.bluetoothchat.feature.chat.group

import com.bluetoothchat.core.ui.navigation.Navigator

interface GroupChatNavigator : Navigator {

    fun navigateToChatInfoScreen(chatId: String)

    fun navigateToUserScreen(userDeviceAddress: String)

    fun navigateToViewImageScreen(chatId: String, messageId: String)

}
