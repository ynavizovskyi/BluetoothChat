package com.bluetoothchat.feature.chat.group

import com.bluetoothchat.core.analytics.AnalyticsClient
import com.bluetoothchat.core.analytics.AnalyticsEvent
import com.bluetoothchat.core.analytics.consts.PROPERTY_BLUETOOTH_PERMISSIONS_GRANTED
import com.bluetoothchat.core.analytics.consts.PROPERTY_MESSAGES_COUNT
import com.bluetoothchat.core.analytics.consts.PROPERTY_SOURCE
import com.bluetoothchat.core.analytics.consts.SOURCE_GROUP_CHAT
import com.bluetoothchat.core.analytics.consts.createBluetoothEnabledEvent
import com.bluetoothchat.core.analytics.consts.createBluetoothPermissionDeniedEvent
import com.bluetoothchat.core.analytics.consts.createBluetoothPermissionGrantedEvent
import com.bluetoothchat.core.analytics.consts.createConnectErrorDialogShownEvent
import com.bluetoothchat.core.analytics.toPropertyMap
import com.bluetoothchat.core.domain.model.Chat
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GroupChatAnalyticsClient @Inject constructor(private val analyticsClient: AnalyticsClient) {

    suspend fun reportScreenShown(chat: Chat.Group, source: String, messagesCount: Int) {
        val event = AnalyticsEvent(
            name = EVENT_SCREEN_SHOWN,
            params = mapOf(PROPERTY_SOURCE to source, PROPERTY_MESSAGES_COUNT to messagesCount) + chat.toPropertyMap()
        )
        analyticsClient.logEvent(event)
    }

    suspend fun reportConnectClicked(source: String) {
        val event = AnalyticsEvent(
            name = EVENT_CONNECT_CLICKED,
            params = mapOf(PROPERTY_SOURCE to source)
        )
        analyticsClient.logEvent(event)
    }

    suspend fun reportBluetoothEnabled() {
        val event = createBluetoothEnabledEvent(source = SOURCE_GROUP_CHAT)
        analyticsClient.logEvent(event)
    }

    suspend fun onBluetoothPermissionGranted() {
        val event = createBluetoothPermissionGrantedEvent(source = SOURCE_GROUP_CHAT)
        analyticsClient.setUserProperty(name = PROPERTY_BLUETOOTH_PERMISSIONS_GRANTED, value = true)
        analyticsClient.logEvent(event)
    }

    suspend fun onBluetoothPermissionDenied() {
        val event = createBluetoothPermissionDeniedEvent(source = SOURCE_GROUP_CHAT)
        analyticsClient.setUserProperty(name = PROPERTY_BLUETOOTH_PERMISSIONS_GRANTED, value = false)
        analyticsClient.logEvent(event)
    }

    suspend fun reportConnectErrorDialogShown() {
        val event = createConnectErrorDialogShownEvent(source = SOURCE_GROUP_CHAT)
        analyticsClient.logEvent(event)
    }

    companion object {
        private const val EVENT_SCREEN_SHOWN = "group_chat_screen_shown"
        private const val EVENT_CONNECT_CLICKED = "connect_clicked"
    }
}
