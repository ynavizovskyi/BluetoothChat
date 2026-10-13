package com.bluetoothchat.feature.chat.group.chatinfo

import androidx.compose.runtime.compositionLocalOf
import com.bluetoothchat.core.ui.navigation.Navigator

val LocalGroupChatInfoNavigator = compositionLocalOf<GroupChatInfoNavigator> { error("GroupChatInfoNavigator not provided") }

interface GroupChatInfoNavigator : Navigator {

    fun navigateToAddUsersScreen(chatId: String)

    fun navigateToUserScreen(userDeviceAddress: String)

}
