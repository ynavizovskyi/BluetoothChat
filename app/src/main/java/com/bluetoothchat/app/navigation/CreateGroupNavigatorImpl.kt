package com.bluetoothchat.app.navigation

import androidx.navigation.NavController
import com.bluetoothchat.core.ui.components.dialog.model.DialogResult
import com.bluetoothchat.core.ui.navigation.BaseNavigator
import com.bluetoothchat.feature.chat.destinations.GroupChatInfoScreenDestination
import com.bluetoothchat.feature.chat.group.chatinfo.GroupChatInfoInputParams
import com.bluetoothchat.feature.connect.group.create.CreateGroupNavigator
import com.bluetoothchat.feature.main.destinations.MainScreenDestination
import com.ramcosta.composedestinations.navigation.navigate
import com.ramcosta.composedestinations.result.OpenResultRecipient

class CreateGroupNavigatorImpl(navController: NavController, dialogResultRecipient: OpenResultRecipient<DialogResult>) :
    BaseNavigator(navController, dialogResultRecipient), CreateGroupNavigator {

    override fun navigateToGroupChatInfoScreen(groupChatId: String) {
        navController.navigate(
            direction = GroupChatInfoScreenDestination(GroupChatInfoInputParams(chatId = groupChatId)),
            navOptionsBuilder = {
                popUpTo(
                    route = MainScreenDestination.route,
                    popUpToBuilder = { inclusive = false }
                )
            },
        )
    }

}

