package com.bluetoothchat.core.bluetooth.message.model.entity

import kotlinx.serialization.Serializable

@Serializable
enum class BtGroupUpdateType {
    GROUP_CREATED, USER_ADDED, USER_REMOVED, USER_LEFT,
}
