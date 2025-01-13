package com.bluetoothchat.feature.connect.group.create

import com.bluetoothchat.core.analytics.AnalyticsClient
import com.bluetoothchat.core.analytics.AnalyticsEvent
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CreateGroupAnalyticsClient @Inject constructor(private val analyticsClient: AnalyticsClient) {

    suspend fun reportScreenShown() {
        val event = AnalyticsEvent(
            name = EVENT_SCREEN_SHOWN,
            params = emptyMap()
        )
        analyticsClient.logEvent(event)
    }

    suspend fun reportSaveError(isProfileImageSet: Boolean) {
        val event = AnalyticsEvent(
            name = EVENT_MY_PROFILE_SAVE_ERROR,
            params = mapOf(
                PROPERTY_GROUP_IMAGE_SET to isProfileImageSet
            )
        )
        analyticsClient.logEvent(event)
    }

    suspend fun reportSaveSuccess(isProfileImageSet: Boolean) {
        val event = AnalyticsEvent(
            name = EVENT_MY_PROFILE_SAVE_SUCCESS,
            params = mapOf(
                PROPERTY_GROUP_IMAGE_SET to isProfileImageSet
            )
        )
        analyticsClient.logEvent(event)
    }

    companion object {
        private const val EVENT_SCREEN_SHOWN = "create_group_screen_shown"
        private const val EVENT_MY_PROFILE_SAVE_ERROR = "create_group_save_error"
        private const val EVENT_MY_PROFILE_SAVE_SUCCESS = "create_group_save_success"

        private const val PROPERTY_GROUP_IMAGE_SET = "group_image_set"
    }
}
