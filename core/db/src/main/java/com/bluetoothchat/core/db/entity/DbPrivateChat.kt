package com.bluetoothchat.core.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "PrivateChat", indices = [Index("userDeviceAddress")])
data class DbPrivateChat(
    @PrimaryKey val userDeviceAddress: String,
    val createdTimestamp: Long,
    //Minor misspelling to avoid keyword conflict
    val exist: Boolean,
)
