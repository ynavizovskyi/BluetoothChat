package com.bluetoothchat.core.db.entity

import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "GroupChatUser",
    primaryKeys = ["chatId", "userDeviceAddress"],
    indices = [Index("chatId", "userDeviceAddress")],
)
data class DbGroupChatUser(
    val chatId: String,
    val userDeviceAddress: String,
)
