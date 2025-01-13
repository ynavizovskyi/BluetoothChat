package com.bluetoothchat.feature.chat.image

import com.bluetoothchat.core.analytics.AnalyticsClient
import com.bluetoothchat.core.analytics.AnalyticsEvent
import com.bluetoothchat.core.analytics.consts.PROPERTY_SOURCE
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ViewImageAnalyticsClient @Inject constructor(private val analyticsClient: AnalyticsClient) {

    suspend fun reportScreenShown(source: String) {
        val event = AnalyticsEvent(
            name = EVENT_SCREEN_SHOWN,
            params = mapOf(PROPERTY_SOURCE to source)
        )
        analyticsClient.logEvent(event)
    }

    companion object {
        private const val EVENT_SCREEN_SHOWN = "view_image_screen_shown"
    }
}
