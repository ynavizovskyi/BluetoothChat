package com.bluetoothchat.core.bluetooth.message.model.entity

import kotlinx.serialization.Serializable

@Serializable
data class BtGroupChatClientInfo(val chatId: String, val exists: Boolean)
