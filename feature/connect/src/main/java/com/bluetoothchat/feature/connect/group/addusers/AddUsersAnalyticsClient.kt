package com.bluetoothchat.feature.connect.group.addusers

import com.bluetoothchat.core.analytics.AnalyticsClient
import com.bluetoothchat.core.analytics.AnalyticsEvent
import com.bluetoothchat.core.analytics.consts.PROPERTY_BLUETOOTH_PERMISSIONS_GRANTED
import com.bluetoothchat.core.analytics.consts.SOURCE_ADD_USERS
import com.bluetoothchat.core.analytics.consts.SOURCE_CONNECT
import com.bluetoothchat.core.analytics.consts.createBluetoothEnabledEvent
import com.bluetoothchat.core.analytics.consts.createBluetoothPermissionDeniedEvent
import com.bluetoothchat.core.analytics.consts.createBluetoothPermissionGrantedEvent
import com.bluetoothchat.core.analytics.consts.createConnectErrorDialogShownEvent
import com.bluetoothchat.core.analytics.toPropertyMap
import com.bluetoothchat.core.domain.model.Chat
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AddUsersAnalyticsClient @Inject constructor(private val analyticsClient: AnalyticsClient) {

    suspend fun reportScreenShown(chat: Chat.Group) {
        val event = AnalyticsEvent(
            name = EVENT_SCREEN_SHOWN,
            params = chat.toPropertyMap()
        )
        analyticsClient.logEvent(event)
    }

    suspend fun reportEnableBluetoothClicked() {
        analyticsClient.logEvent(EVENT_ENABLE_BLUETOOTH_CLICKED)
    }

    suspend fun reportBluetoothEnabled() {
        val event = createBluetoothEnabledEvent(source = SOURCE_CONNECT)
        analyticsClient.logEvent(event)
    }

    suspend fun reportGrantBluetoothPermissionClicked() {
        analyticsClient.logEvent(EVENT_GRANT_BLUETOOTH_PERMISSION_CLICKED)
    }

    suspend fun onBluetoothPermissionGranted() {
        val event = createBluetoothPermissionGrantedEvent(source = SOURCE_ADD_USERS)
        analyticsClient.setUserProperty(name = PROPERTY_BLUETOOTH_PERMISSIONS_GRANTED, value = true)
        analyticsClient.logEvent(event)
    }

    suspend fun onBluetoothPermissionDenied() {
        val event = createBluetoothPermissionDeniedEvent(source = SOURCE_ADD_USERS)
        analyticsClient.setUserProperty(name = PROPERTY_BLUETOOTH_PERMISSIONS_GRANTED, value = false)
        analyticsClient.logEvent(event)
    }

    suspend fun reportConnectErrorDialogShown() {
        val event = createConnectErrorDialogShownEvent(source = SOURCE_ADD_USERS)
        analyticsClient.logEvent(event)
    }

    suspend fun reportMemberAdded() {
        analyticsClient.logEvent(name = EVENT_MEMBER_ADDED)
    }

    companion object {
        private const val EVENT_SCREEN_SHOWN = "add_users_screen_shown"
        private const val EVENT_ENABLE_BLUETOOTH_CLICKED = "enable_bluetooth_clicked"
        private const val EVENT_GRANT_BLUETOOTH_PERMISSION_CLICKED = "grant_bluetooth_permission_clicked"
        private const val EVENT_MEMBER_ADDED = "member_added"
    }
}
