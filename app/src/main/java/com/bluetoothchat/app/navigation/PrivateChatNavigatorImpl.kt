package com.bluetoothchat.app.navigation

import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.bluetoothchat.core.analytics.consts.SOURCE_PRIVATE_CHAT
import com.bluetoothchat.feature.chat.image.ViewImageInputParams
import com.bluetoothchat.feature.chat.privat.PrivateChatNavigator
import com.bluetoothchat.feature.profile.ProfileInputParams
import com.bluetoothchat.feature.profile.ProfileLaunchMode

class PrivateChatNavigatorImpl(backStack: NavBackStack<NavKey>) : BaseNavigator(backStack), PrivateChatNavigator {

    override fun navigateToViewImageScreen(chatId: String, messageId: String) {
        backStack.add(
            NavRoutes.ViewImage(
                ViewImageInputParams(
                    chatId = chatId,
                    messageId = messageId,
                    source = SOURCE_PRIVATE_CHAT,
                )
            )
        )
    }

    override fun navigateToUserScreen(userDeviceAddress: String) {
        backStack.add(
            NavRoutes.Profile(
                ProfileInputParams(
                    mode = ProfileLaunchMode.Other(userDeviceAddress),
                    source = SOURCE_PRIVATE_CHAT,
                )
            )
        )
    }

}
