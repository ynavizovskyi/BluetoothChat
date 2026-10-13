package com.bluetoothchat.app.navigation

import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.bluetoothchat.feature.chat.group.chatinfo.GroupChatInfoInputParams
import com.bluetoothchat.feature.connect.group.create.CreateGroupNavigator

class CreateGroupNavigatorImpl(backStack: NavBackStack<NavKey>) : BaseNavigator(backStack), CreateGroupNavigator {

    override fun navigateToGroupChatInfoScreen(groupChatId: String) {
        backStack.add(NavRoutes.GroupChatInfo(GroupChatInfoInputParams(chatId = groupChatId)))
        backStack.clearUpTo<NavRoutes.Main>()
    }

}
