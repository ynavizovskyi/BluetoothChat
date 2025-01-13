package com.bluetoothchat.core.bluetooth.message.model.entity

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed interface BtMessage {
    val id: String
    val userDeviceAddress: String
    val timestamp: Long
    val isReadByMe: Boolean

    @SerialName("messageGroupUpdate")
    @Serializable
    data class GroupUpdate(
        override val id: String,
        override val userDeviceAddress: String,
        override val timestamp: Long,
        override val isReadByMe: Boolean,
        val updateType: BtGroupUpdateType,
        val targetDeviceAddress: String?,
    ) : BtMessage

    @SerialName("messagePlain")
    @Serializable
    data class Plain(
        override val id: String,
        override val userDeviceAddress: String,
        override val timestamp: Long,
        override val isReadByMe: Boolean,
        val quotedMessageId: String?,
        val content: List<BtMessageContent>,
    ) : BtMessage

}
