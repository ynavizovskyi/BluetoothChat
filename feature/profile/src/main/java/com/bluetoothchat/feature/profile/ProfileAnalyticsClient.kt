package com.bluetoothchat.feature.profile

import com.bluetoothchat.core.analytics.AnalyticsClient
import com.bluetoothchat.core.analytics.AnalyticsEvent
import com.bluetoothchat.core.analytics.consts.PROPERTY_SOURCE
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProfileAnalyticsClient @Inject constructor(private val analyticsClient: AnalyticsClient) {

    suspend fun reportScreenShownMyProfile(isInitialSetup: Boolean, source: String) {
        val event = AnalyticsEvent(
            name = EVENT_SCREEN_SHOWN_MY_PROFILE,
            params = mapOf(PROPERTY_IS_INITIAL_SETUP to isInitialSetup, PROPERTY_SOURCE to source)
        )
        analyticsClient.logEvent(event)
    }

    suspend fun reportScreenShownOthersProfile(source: String) {
        val event = AnalyticsEvent(
            name = EVENT_SCREEN_SHOWN_OTHERS_PROFILE,
            params = mapOf(PROPERTY_SOURCE to source)
        )
        analyticsClient.logEvent(event)
    }

    suspend fun reportEditClicked() {
        val event = AnalyticsEvent(
            name = EVENT_EDIT_CLICKED,
            params = emptyMap()
        )
        analyticsClient.logEvent(event)
    }

    suspend fun reportSaveError(isInitialSetup: Boolean, source: String, isProfileImageSet: Boolean) {
        val event = AnalyticsEvent(
            name = EVENT_MY_PROFILE_SAVE_ERROR,
            params = mapOf(
                PROPERTY_IS_INITIAL_SETUP to isInitialSetup,
                PROPERTY_SOURCE to source,
                PROPERTY_PROFILE_IMAGE_SET to isProfileImageSet
            )
        )
        analyticsClient.logEvent(event)
    }

    suspend fun reportSaveSuccess(isInitialSetup: Boolean, source: String, isProfileImageSet: Boolean) {
        val event = AnalyticsEvent(
            name = EVENT_MY_PROFILE_SAVE_SUCCESS,
            params = mapOf(
                PROPERTY_IS_INITIAL_SETUP to isInitialSetup,
                PROPERTY_SOURCE to source,
                PROPERTY_PROFILE_IMAGE_SET to isProfileImageSet
            )
        )
        analyticsClient.logEvent(event)
    }

    companion object {
        private const val EVENT_SCREEN_SHOWN_OTHERS_PROFILE = "others_profile_screen_shown"
        private const val EVENT_SCREEN_SHOWN_MY_PROFILE = "my_profile_screen_shown"
        private const val EVENT_MY_PROFILE_SAVE_ERROR = "my_profile_save_error"
        private const val EVENT_MY_PROFILE_SAVE_SUCCESS = "my_profile_save_success"
        private const val EVENT_EDIT_CLICKED = "my_profile_edit_clicked"

        private const val PROPERTY_IS_INITIAL_SETUP = "is_initial_setup"
        private const val PROPERTY_PROFILE_IMAGE_SET = "profile_image_set"
    }
}
