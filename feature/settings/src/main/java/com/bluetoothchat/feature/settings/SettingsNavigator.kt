package com.bluetoothchat.feature.settings

import androidx.compose.runtime.compositionLocalOf
import com.bluetoothchat.core.ui.navigation.Navigator

val LocalSettingsNavigator = compositionLocalOf<SettingsNavigator> { error("SettingsNavigator not provided") }

interface SettingsNavigator : Navigator {

}
