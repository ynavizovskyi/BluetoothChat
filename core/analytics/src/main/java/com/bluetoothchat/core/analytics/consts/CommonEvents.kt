package com.bluetoothchat.core.analytics.consts

import com.bluetoothchat.core.analytics.AnalyticsEvent

private const val EVENT_ERROR_DIALOG_SHOWN = "error_dialog_shown"
private const val EVENT_CONNECT_ERROR_DIALOG_SHOWN = "connect_error_dialog_shown"
private const val EVENT_TERMS_OF_USE_CLICKED = "terms_of_use_clicked"
private const val EVENT_PRIVACY_POLICY_CLICKED = "privacy_policy_clicked"
private const val EVENT_BLUETOOTH_ENABLED = "bluetooth_enabled"
private const val EVENT_BLUETOOTH_PERMISSION_GRANTED = "bluetooth_permission_granted"
private const val EVENT_BLUETOOTH_PERMISSION_DENIED = "bluetooth_permission_denied"
private const val EVENT_NOTIFICATION_PERMISSION_GRANTED = "notification_permission_granted"
private const val EVENT_NOTIFICATION_PERMISSION_DENIED = "notification_permission_denied"

private const val PROPERTY_CAUSE = "cause"
const val PROPERTY_SOURCE = "source"
const val PROPERTY_MESSAGES_COUNT = "messages_count"
const val PROPERTY_THEME = "theme"
const val PROPERTY_BLUETOOTH_ENABLED = "bluetooth_enabled"
const val PROPERTY_BLUETOOTH_PERMISSIONS_GRANTED = "bluetooth_permissions_granted"
const val PROPERTY_NOTIFICATION_PERMISSION_GRANTED = "notification_permission_granted"
const val PROPERTY_CHAT_COUNT = "chat_count"

fun createErrorDialogShownEvent(source: String, cause: String) = AnalyticsEvent(
    name = EVENT_ERROR_DIALOG_SHOWN,
    params = mapOf(PROPERTY_SOURCE to source, PROPERTY_CAUSE to cause),
)

fun createConnectErrorDialogShownEvent(source: String) = AnalyticsEvent(
    name = EVENT_CONNECT_ERROR_DIALOG_SHOWN,
    params = mapOf(PROPERTY_SOURCE to source),
)

fun createTermsOfUseClickedEvent(source: String) = AnalyticsEvent(
    name = EVENT_TERMS_OF_USE_CLICKED,
    params = mapOf(PROPERTY_SOURCE to source),
)

fun createPrivacyPolicyClickedEvent(source: String) = AnalyticsEvent(
    name = EVENT_PRIVACY_POLICY_CLICKED,
    params = mapOf(PROPERTY_SOURCE to source),
)

fun createBluetoothEnabledEvent(source: String) = AnalyticsEvent(
    name = EVENT_BLUETOOTH_ENABLED,
    params = mapOf(PROPERTY_SOURCE to source),
)

fun createBluetoothPermissionGrantedEvent(source: String) = AnalyticsEvent(
    name = EVENT_BLUETOOTH_PERMISSION_GRANTED,
    params = mapOf(PROPERTY_SOURCE to source),
)

fun createBluetoothPermissionDeniedEvent(source: String) = AnalyticsEvent(
    name = EVENT_BLUETOOTH_PERMISSION_DENIED,
    params = mapOf(PROPERTY_SOURCE to source),
)

fun createNotificationPermissionGrantedEvent(source: String) = AnalyticsEvent(
    name = EVENT_NOTIFICATION_PERMISSION_GRANTED,
    params = mapOf(PROPERTY_SOURCE to source),
)

fun createNotificationPermissionDeniedEvent(source: String) = AnalyticsEvent(
    name = EVENT_NOTIFICATION_PERMISSION_DENIED,
    params = mapOf(PROPERTY_SOURCE to source),
)
