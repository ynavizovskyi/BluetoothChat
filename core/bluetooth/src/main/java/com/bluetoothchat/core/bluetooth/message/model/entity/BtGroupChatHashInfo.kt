package com.bluetoothchat.core.bluetooth.message.model.entity

import kotlinx.serialization.Serializable

@Serializable
data class BtGroupChatHashInfo(
    val chatId: String,
    val chatHash: Int,
    val chatLastMessageId: String?,
)
