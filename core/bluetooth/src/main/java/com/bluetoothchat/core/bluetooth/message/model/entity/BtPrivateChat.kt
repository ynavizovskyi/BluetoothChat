package com.bluetoothchat.core.bluetooth.message.model.entity

import kotlinx.serialization.Serializable

@Serializable
data class BtPrivateChat(
    val user: BtUser,
    val createdTimestamp: Long,
)
