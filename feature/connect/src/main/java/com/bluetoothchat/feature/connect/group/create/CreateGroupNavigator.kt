package com.bluetoothchat.feature.connect.group.create

import com.bluetoothchat.core.ui.navigation.Navigator

interface CreateGroupNavigator : Navigator {

    fun navigateToGroupChatInfoScreen(groupChatId: String)

}
