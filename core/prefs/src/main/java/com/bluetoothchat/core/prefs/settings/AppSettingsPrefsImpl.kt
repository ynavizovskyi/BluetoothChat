package com.bluetoothchat.core.prefs.settings

import android.content.Context
import com.bluetoothchat.core.dispatcher.DispatcherManager
import com.bluetoothchat.core.prefs.settings.model.ChatAppTheme
import com.fredporciuncula.flow.preferences.FlowSharedPreferences
import kotlinx.coroutines.flow.Flow

class AppSettingsPrefsImpl(
    context: Context,
    dispatcherManager: DispatcherManager,
) : AppSettingsPrefs {

    private val preferences = context.getSharedPreferences("app_settings_prefs", Context.MODE_PRIVATE)
    private val flowPreferences = FlowSharedPreferences(preferences, dispatcherManager.io)

    override fun setAppTheme(theme: ChatAppTheme) =
        flowPreferences.getEnum(KEY_APP_THEME, DEFAULT_APP_THEME).set(theme)

    override fun getAppTheme(): ChatAppTheme =
        flowPreferences.getEnum(KEY_APP_THEME, DEFAULT_APP_THEME).get()

    override fun observeAppTheme(): Flow<ChatAppTheme> =
        flowPreferences.getEnum(KEY_APP_THEME, DEFAULT_APP_THEME).asFlow()

    companion object {
        private const val KEY_APP_THEME = "app_theme"
        private val DEFAULT_APP_THEME = ChatAppTheme.SYSTEM
    }
}
