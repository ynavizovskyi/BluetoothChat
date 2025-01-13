package com.bluetoothchat.core.bluetooth.message.model.entity

import kotlinx.serialization.Serializable

@Serializable
data class BtGroupChat(
    val id: String,
    val createdTimestamp: Long,
    val exists: Boolean,
    val hostDeviceAddress: String,
    val name: String,
    val picture: BtPicture?,
    val users: List<BtUser>,
    //Property for the future connection state sync to avoid having to update contract
    val connectedDevicesIds: Set<String>,
)
