package com.bluetoothchat.feature.connect.group.create

import androidx.compose.runtime.compositionLocalOf
import com.bluetoothchat.core.ui.navigation.Navigator

val LocalCreateGroupNavigator = compositionLocalOf<CreateGroupNavigator> { error("CreateGroupNavigator not provided") }

interface CreateGroupNavigator : Navigator {

    fun navigateToGroupChatInfoScreen(groupChatId: String)

}
