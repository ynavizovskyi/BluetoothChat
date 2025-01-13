package com.bluetoothchat.core.bluetooth.message.model.entity

import kotlinx.serialization.Serializable

@Serializable
data class BtPicture(
    val id: String,
    val sizeBytes: Long,
)
