package com.bluetoothchat.core.db.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "User", indices = [Index("deviceAddress")])
data class DbUser(
    @PrimaryKey val deviceAddress: String,
    val color: Int,
    val deviceName: String?,
    val userName: String?,
    @Embedded val picture: DbPicture?,
)
