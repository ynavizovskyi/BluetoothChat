package com.bluetoothchat.app.navigation

import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.bluetoothchat.core.analytics.consts.SOURCE_GROUP_CHAT_INFO
import com.bluetoothchat.feature.chat.group.chatinfo.GroupChatInfoNavigator
import com.bluetoothchat.feature.connect.group.addusers.AddUsersInputParams
import com.bluetoothchat.feature.profile.ProfileInputParams
import com.bluetoothchat.feature.profile.ProfileLaunchMode

class GroupChatInfoNavigatorImpl(backStack: NavBackStack<NavKey>) : BaseNavigator(backStack), GroupChatInfoNavigator {

    override fun navigateToAddUsersScreen(chatId: String) {
        backStack.add(NavRoutes.AddUsers(AddUsersInputParams(chatId = chatId)))
    }

    override fun navigateToUserScreen(userDeviceAddress: String) {
        backStack.add(
            NavRoutes.Profile(
                ProfileInputParams(
                    mode = ProfileLaunchMode.Other(userDeviceAddress),
                    source = SOURCE_GROUP_CHAT_INFO,
                )
            )
        )
    }

}
