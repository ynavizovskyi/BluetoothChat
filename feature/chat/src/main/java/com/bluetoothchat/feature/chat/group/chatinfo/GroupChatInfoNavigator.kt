package com.bluetoothchat.feature.chat.group.chatinfo

import com.bluetoothchat.core.ui.navigation.Navigator

interface GroupChatInfoNavigator : Navigator {

    fun navigateToAddUsersScreen(chatId: String)

    fun navigateToUserScreen(userDeviceAddress: String)

}
