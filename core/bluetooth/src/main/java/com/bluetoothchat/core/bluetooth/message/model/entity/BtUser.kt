package com.bluetoothchat.core.bluetooth.message.model.entity

import kotlinx.serialization.Serializable

@Serializable
data class BtUser(
    val deviceAddress: String,
    val color: Int,
    val deviceName: String?,
    val userName: String?,
    val picture: BtPicture?,
)
