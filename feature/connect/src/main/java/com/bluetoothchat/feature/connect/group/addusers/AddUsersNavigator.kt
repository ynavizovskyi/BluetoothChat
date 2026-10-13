package com.bluetoothchat.feature.connect.group.addusers

import androidx.compose.runtime.compositionLocalOf
import com.bluetoothchat.core.ui.navigation.Navigator

val LocalAddUsersNavigator = compositionLocalOf<AddUsersNavigator> { error("AddUsersNavigator not provided") }

interface AddUsersNavigator : Navigator {
}
