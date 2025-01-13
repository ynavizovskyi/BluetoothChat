package com.bluetoothchat.core.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.bluetoothchat.core.db.entity.DbGroupChat
import com.bluetoothchat.core.db.entity.DbGroupChatUser
import com.bluetoothchat.core.db.entity.DbMessage
import com.bluetoothchat.core.db.entity.DbPrivateChat
import com.bluetoothchat.core.db.entity.DbUser

@Database(
    entities = [DbUser::class, DbMessage::class, DbPrivateChat::class, DbGroupChat::class, DbGroupChatUser::class],
    version = 20
)
abstract class RoomDatabaseManager : RoomDatabase(), DatabaseManager {

    companion object {
        fun create(context: Context): DatabaseManager {
            return Room
                .databaseBuilder(context, RoomDatabaseManager::class.java, "bluetooth_chat")
                .fallbackToDestructiveMigration()
                .build()
        }
    }
}
