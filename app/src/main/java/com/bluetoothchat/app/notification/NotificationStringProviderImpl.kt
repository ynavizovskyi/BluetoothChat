package com.bluetoothchat.app.notification

import android.content.Context
import com.bluetoothchat.core.bluetooth.notification.NotificationStringProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import com.bluetoothchat.core.ui.R as CoreUiR

@Singleton
class NotificationStringProviderImpl @Inject constructor(
    @ApplicationContext private val context: Context,
) : NotificationStringProvider {

    override fun getOngoingNotificationTitle(): String =
        context.getString(CoreUiR.string.notification_is_running, context.getString(CoreUiR.string.app_name))

    override fun getOngoingChannelName(): String =
        context.getString(CoreUiR.string.notification_is_running, context.getString(CoreUiR.string.app_name))

    override fun getMessageChannelName(): String =
        context.getString(CoreUiR.string.notification_channel_messages)

    override fun getPrivateChatStarted(name: String): String =
        context.getString(CoreUiR.string.notification_private_chat_started, name)

    override fun getAddedToGroupChat(name: String): String =
        context.getString(CoreUiR.string.notification_added_to_group, name)

    override fun getConnectedDevicesMessage(devices: String): String =
        context.getString(CoreUiR.string.notification_connected_devices, devices)

    override fun getNoConnectedDevicesMessage(): String =
        context.getString(CoreUiR.string.notification_no_connected_devices)

    override fun getStop(): String =
        context.getString(CoreUiR.string.notification_stop)

}
