package com.bluetoothchat.feature.chat.privat

import androidx.compose.runtime.compositionLocalOf
import com.bluetoothchat.core.ui.navigation.Navigator

val LocalPrivateChatNavigator = compositionLocalOf<PrivateChatNavigator> { error("PrivateChatNavigator not provided") }

interface PrivateChatNavigator : Navigator {

    fun navigateToViewImageScreen(chatId: String, messageId: String)

    fun navigateToUserScreen(userDeviceAddress: String)

}
