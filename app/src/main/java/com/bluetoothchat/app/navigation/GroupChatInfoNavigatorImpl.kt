package com.bluetoothchat.app.navigation

import androidx.navigation.NavController
import com.bluetoothchat.core.analytics.consts.SOURCE_GROUP_CHAT_INFO
import com.bluetoothchat.core.ui.components.dialog.model.DialogResult
import com.bluetoothchat.core.ui.navigation.BaseNavigator
import com.bluetoothchat.feature.chat.group.chatinfo.GroupChatInfoNavigator
import com.bluetoothchat.feature.connect.destinations.AddUsersScreenDestination
import com.bluetoothchat.feature.connect.group.addusers.AddUsersInputParams
import com.bluetoothchat.feature.profile.ProfileInputParams
import com.bluetoothchat.feature.profile.ProfileLaunchMode
import com.bluetoothchat.feature.profile.destinations.ProfileScreenDestination
import com.ramcosta.composedestinations.navigation.navigate
import com.ramcosta.composedestinations.result.OpenResultRecipient

class GroupChatInfoNavigatorImpl(
    navController: NavController,
    dialogResultRecipient: OpenResultRecipient<DialogResult>
) : BaseNavigator(navController, dialogResultRecipient), GroupChatInfoNavigator {

    override fun navigateToAddUsersScreen(chatId: String) {
        navController.navigate(direction = AddUsersScreenDestination(AddUsersInputParams(chatId = chatId)))
    }

    override fun navigateToUserScreen(userDeviceAddress: String) {
        navController.navigate(
            direction = ProfileScreenDestination(
                ProfileInputParams(
                    mode = ProfileLaunchMode.Other(userDeviceAddress),
                    source = SOURCE_GROUP_CHAT_INFO,
                )
            )
        )
    }

}
