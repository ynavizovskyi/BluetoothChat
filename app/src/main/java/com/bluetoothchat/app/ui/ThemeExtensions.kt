package com.bluetoothchat.app.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import com.bluetoothchat.core.prefs.settings.AppSettingsPrefs
import com.bluetoothchat.core.prefs.settings.model.ChatAppTheme

@Composable
fun AppSettingsPrefs.shouldUseDarkColors(): Boolean {
    val themePreference = remember { observeAppTheme() }.collectAsState(initial = getAppTheme())
    return when (themePreference.value) {
        ChatAppTheme.SYSTEM -> isSystemInDarkTheme()
        ChatAppTheme.LIGHT -> false
        ChatAppTheme.DARK -> true
    }
}
