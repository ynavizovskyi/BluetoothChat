package com.bluetoothchat.feature.main

import com.bluetoothchat.core.analytics.AnalyticsClient
import com.bluetoothchat.core.analytics.AnalyticsEvent
import com.bluetoothchat.core.analytics.consts.PROPERTY_BLUETOOTH_PERMISSIONS_GRANTED
import com.bluetoothchat.core.analytics.consts.PROPERTY_CHAT_COUNT
import com.bluetoothchat.core.analytics.consts.PROPERTY_NOTIFICATION_PERMISSION_GRANTED
import com.bluetoothchat.core.analytics.consts.SOURCE_MAIN
import com.bluetoothchat.core.analytics.consts.createBluetoothPermissionDeniedEvent
import com.bluetoothchat.core.analytics.consts.createBluetoothPermissionGrantedEvent
import com.bluetoothchat.core.analytics.consts.createNotificationPermissionDeniedEvent
import com.bluetoothchat.core.analytics.consts.createNotificationPermissionGrantedEvent
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MainAnalyticsClient @Inject constructor(private val analyticsClient: AnalyticsClient) {

    suspend fun reportScreenShown(chatCount: Int) {
        analyticsClient.logEvent(name = EVENT_SCREEN_SHOWN, params = mapOf(PROPERTY_CHAT_COUNT to chatCount))
    }

    suspend fun onBluetoothPermissionsGranted() {
        val event = createBluetoothPermissionGrantedEvent(source = SOURCE_MAIN)
        analyticsClient.setUserProperty(name = PROPERTY_BLUETOOTH_PERMISSIONS_GRANTED, value = true)
        analyticsClient.logEvent(event)
    }

    suspend fun onBluetoothPermissionsDenied() {
        val event = createBluetoothPermissionDeniedEvent(source = SOURCE_MAIN)
        analyticsClient.setUserProperty(name = PROPERTY_BLUETOOTH_PERMISSIONS_GRANTED, value = false)
        analyticsClient.logEvent(event)
    }

    suspend fun onNotificationPermissionGranted() {
        val event = createNotificationPermissionGrantedEvent(source = SOURCE_MAIN)
        analyticsClient.setUserProperty(name = PROPERTY_NOTIFICATION_PERMISSION_GRANTED, value = true)
        analyticsClient.logEvent(event)
    }

    suspend fun onNotificationPermissionDenied() {
        val event = createNotificationPermissionDeniedEvent(source = SOURCE_MAIN)
        analyticsClient.setUserProperty(name = PROPERTY_NOTIFICATION_PERMISSION_GRANTED, value = false)
        analyticsClient.logEvent(event)
    }

    suspend fun reportOnLaunchInAppReviewSuccess() = analyticsClient.logEvent(EVENT_ON_LAUNCH_IN_APP_REVIEW_SUCCESS)
    suspend fun reportOnLaunchInAppReviewError() = analyticsClient.logEvent(EVENT_ON_LAUNCH_IN_APP_REVIEW_ERROR)

    suspend fun reportRateAppDialogShown() = analyticsClient.logEvent(EVENT_RATE_APP_DIALOG_SHOWN)

    suspend fun reportRateAppNeverAskOptionSelected() = reportRateAppDialogOptionSelected("never_ask")
    suspend fun reportRateAppRateOptionSelected() = reportRateAppDialogOptionSelected("rate")
    suspend fun reportRateAppNoneOptionSelected() = reportRateAppDialogOptionSelected("none")

    private suspend fun reportRateAppDialogOptionSelected(option: String) {
        val event = AnalyticsEvent(
            name = EVENT_RATE_APP_DIALOG_OPTION_SELECTED,
            params = mapOf(PROPERTY_RATE_APP_DIALOG_OPTION to option),
        )

        analyticsClient.logEvent(event)
    }

    companion object {
        private const val EVENT_SCREEN_SHOWN = "main_screen_shown"
        private const val EVENT_RATE_APP_DIALOG_SHOWN = "rate_app_dialog_shown"
        private const val EVENT_RATE_APP_DIALOG_OPTION_SELECTED = "rate_app_dialog_option_selected"
        private const val EVENT_ON_LAUNCH_IN_APP_REVIEW_SUCCESS = "on_launch_in_app_review_success"
        private const val EVENT_ON_LAUNCH_IN_APP_REVIEW_ERROR = "on_launch_in_app_review_error"

        private const val PROPERTY_RATE_APP_DIALOG_OPTION = "rate_app_dialog_option"
    }
}
