package com.bluetoothchat.feature.settings

import com.bluetoothchat.core.prefs.settings.model.ChatAppTheme
import com.bluetoothchat.core.ui.R as CoreUiR

fun ChatAppTheme.nameResId() = when (this) {
    ChatAppTheme.SYSTEM -> CoreUiR.string.theme_system
    ChatAppTheme.LIGHT -> CoreUiR.string.theme_light
    ChatAppTheme.DARK -> CoreUiR.string.theme_dark
}
