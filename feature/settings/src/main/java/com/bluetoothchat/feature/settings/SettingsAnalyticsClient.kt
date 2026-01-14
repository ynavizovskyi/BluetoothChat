package com.bluetoothchat.feature.settings

import com.bluetoothchat.core.analytics.AnalyticsClient
import com.bluetoothchat.core.analytics.AnalyticsEvent
import com.bluetoothchat.core.analytics.consts.PROPERTY_THEME
import com.bluetoothchat.core.analytics.consts.createPrivacyPolicyClickedEvent
import com.bluetoothchat.core.analytics.consts.createTermsOfUseClickedEvent
import com.bluetoothchat.core.analytics.toAnalyticsValue
import com.bluetoothchat.core.prefs.settings.model.ChatAppTheme
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsAnalyticsClient @Inject constructor(private val analyticsClient: AnalyticsClient) {

    suspend fun reportScreenShown() = analyticsClient.logEvent(EVENT_SCREEN_SHOWN)

    suspend fun reportChangeThemeClicked() = analyticsClient.logEvent(EVENT_CHANGE_THEME_CLICKED)

    suspend fun reportThemeChanged(theme: ChatAppTheme) {
        val event = AnalyticsEvent(
            name = EVENT_THEME_CHANGED,
            params = mapOf(PROPERTY_THEME to theme.toAnalyticsValue()),
        )
        analyticsClient.logEvent(event = event)
    }

    suspend fun reportTermsOfUseClicked() {
        val event = createTermsOfUseClickedEvent(source = PROPERTY_VALUE_SCREEN_SOURCE)
        analyticsClient.logEvent(event = event)
    }

    suspend fun reportPrivacyPolicyClicked() {
        val event = createPrivacyPolicyClickedEvent(source = PROPERTY_VALUE_SCREEN_SOURCE)
        analyticsClient.logEvent(event = event)
    }

    companion object {
        private const val PROPERTY_VALUE_SCREEN_SOURCE = "settings"

        private const val EVENT_SCREEN_SHOWN = "settings_screen_shown"
        private const val EVENT_CHANGE_THEME_CLICKED = "settings_change_theme_clicked"
        private const val EVENT_THEME_CHANGED = "settings_theme_changed"
    }
}
