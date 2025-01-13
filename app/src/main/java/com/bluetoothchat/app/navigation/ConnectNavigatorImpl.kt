package com.bluetoothchat.app.navigation

import androidx.navigation.NavController
import com.bluetoothchat.core.analytics.consts.SOURCE_CONNECT
import com.bluetoothchat.core.ui.components.dialog.model.DialogResult
import com.bluetoothchat.core.ui.navigation.BaseNavigator
import com.bluetoothchat.feature.chat.destinations.GroupChatScreenDestination
import com.bluetoothchat.feature.chat.destinations.PrivateChatScreenDestination
import com.bluetoothchat.feature.chat.group.GroupChatInputParams
import com.bluetoothchat.feature.chat.privat.PrivateChatInputParams
import com.bluetoothchat.feature.connect.destinations.CreateGroupScreenDestination
import com.bluetoothchat.feature.connect.main.ConnectNavigator
import com.ramcosta.composedestinations.navigation.navigate
import com.ramcosta.composedestinations.result.OpenResultRecipient

class ConnectNavigatorImpl(navController: NavController, dialogResultRecipient: OpenResultRecipient<DialogResult>) :
    BaseNavigator(navController, dialogResultRecipient), ConnectNavigator {

    override fun navigateToCreateGroupScreen() {
        navController.navigate(CreateGroupScreenDestination)
    }

    override fun navigateToPrivateChatScreen(chatId: String) {
        navController.navigate(
            direction = PrivateChatScreenDestination(PrivateChatInputParams(chatId = chatId, source = SOURCE_CONNECT)),
        )
    }

    override fun navigateToGroupChatScreen(chatId: String) {
        navController.navigate(
            direction = GroupChatScreenDestination(GroupChatInputParams(chatId = chatId, source = SOURCE_CONNECT)),
        )
    }
}

