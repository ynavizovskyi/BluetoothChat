package com.bluetoothchat.feature.chat.privat

import com.bluetoothchat.core.ui.navigation.Navigator

interface PrivateChatNavigator : Navigator {

    fun navigateToViewImageScreen(chatId: String, messageId: String)

    fun navigateToUserScreen(userDeviceAddress: String)

}
