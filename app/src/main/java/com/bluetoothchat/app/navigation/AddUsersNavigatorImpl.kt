package com.bluetoothchat.app.navigation

import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.bluetoothchat.feature.connect.group.addusers.AddUsersNavigator

class AddUsersNavigatorImpl(backStack: NavBackStack<NavKey>) : BaseNavigator(backStack), AddUsersNavigator {

}
