package com.bluetoothchat.app.navigation

import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.bluetoothchat.core.analytics.consts.SOURCE_GROUP_CHAT
import com.bluetoothchat.feature.chat.group.GroupChatNavigator
import com.bluetoothchat.feature.chat.group.chatinfo.GroupChatInfoInputParams
import com.bluetoothchat.feature.chat.image.ViewImageInputParams
import com.bluetoothchat.feature.profile.ProfileInputParams
import com.bluetoothchat.feature.profile.ProfileLaunchMode

class GroupChatNavigatorImpl(backStack: NavBackStack<NavKey>) : BaseNavigator(backStack), GroupChatNavigator {

    override fun navigateToChatInfoScreen(chatId: String) {
        backStack.add(NavRoutes.GroupChatInfo(GroupChatInfoInputParams(chatId = chatId)))
    }

    override fun navigateToUserScreen(userDeviceAddress: String) {
        backStack.add(
            NavRoutes.Profile(
                ProfileInputParams(
                    mode = ProfileLaunchMode.Other(userDeviceAddress),
                    source = SOURCE_GROUP_CHAT,
                )
            )
        )
    }

    override fun navigateToViewImageScreen(chatId: String, messageId: String) {
        backStack.add(
            NavRoutes.ViewImage(
                ViewImageInputParams(
                    chatId = chatId,
                    messageId = messageId,
                    source = SOURCE_GROUP_CHAT,
                )
            )
        )
    }
}
