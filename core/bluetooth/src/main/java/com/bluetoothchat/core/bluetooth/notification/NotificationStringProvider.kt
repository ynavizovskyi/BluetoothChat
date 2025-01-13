package com.bluetoothchat.core.bluetooth.notification

interface NotificationStringProvider {

    fun getOngoingNotificationTitle(): String

    fun getOngoingChannelName(): String

    fun getMessageChannelName(): String

    fun getPrivateChatStarted(name: String): String

    fun getAddedToGroupChat(name: String): String

    fun getConnectedDevicesMessage(devices: String): String

    fun getNoConnectedDevicesMessage(): String

    fun getStop(): String

}
