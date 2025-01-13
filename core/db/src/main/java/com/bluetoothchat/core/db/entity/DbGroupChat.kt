package com.bluetoothchat.core.db.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "GroupChat", indices = [Index("id")])
data class DbGroupChat(
    @PrimaryKey val id: String,
    val createdTimestamp: Long,
    //Minor misspelling to avoid keyword conflict
    val exist: Boolean,
    val hostDeviceAddress: String,
    val name: String,
    @Embedded val picture: DbPicture?,
)
