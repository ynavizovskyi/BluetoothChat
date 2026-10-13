package com.bluetoothchat.app.navigation

import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.bluetoothchat.feature.settings.SettingsNavigator

class SettingsNavigatorImpl(backStack: NavBackStack<NavKey>) : BaseNavigator(backStack), SettingsNavigator {

}
