package com.bluetoothchat.core.db.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "Message",
    indices = [Index("id"), Index("chatId"), Index("timestamp"), Index("chatId", "timestamp")]
)
data class DbMessage(
    @PrimaryKey val id: String,
    val chatId: String,
    val userDeviceAddress: String,
    val timestamp: Long,
    val isReadByMe: Boolean,
    @Embedded val groupUpdateData: DbGroupUpdateMessageData?,
    @Embedded val plainData: DbPlainMessageData?,
)
