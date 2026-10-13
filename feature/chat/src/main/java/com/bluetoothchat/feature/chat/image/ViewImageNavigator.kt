package com.bluetoothchat.feature.chat.image

import androidx.compose.runtime.compositionLocalOf
import com.bluetoothchat.core.ui.navigation.Navigator

val LocalViewImageNavigator = compositionLocalOf<ViewImageNavigator> { error("ViewImageNavigator not provided") }

interface ViewImageNavigator : Navigator {
}
