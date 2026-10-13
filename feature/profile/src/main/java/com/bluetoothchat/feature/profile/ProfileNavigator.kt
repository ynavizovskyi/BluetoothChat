package com.bluetoothchat.feature.profile

import androidx.compose.runtime.compositionLocalOf
import com.bluetoothchat.core.ui.navigation.Navigator

val LocalProfileNavigator = compositionLocalOf<ProfileNavigator> { error("ProfileNavigator not provided") }

interface ProfileNavigator : Navigator {

    fun navigateToMain()

}
