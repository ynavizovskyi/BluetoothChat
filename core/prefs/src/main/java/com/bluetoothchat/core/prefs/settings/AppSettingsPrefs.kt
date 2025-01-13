package com.bluetoothchat.core.prefs.settings

import com.bluetoothchat.core.prefs.settings.model.ChatAppTheme
import kotlinx.coroutines.flow.Flow

interface AppSettingsPrefs {

    fun setAppTheme(theme: ChatAppTheme)

    fun getAppTheme(): ChatAppTheme

    fun observeAppTheme(): Flow<ChatAppTheme>

}
