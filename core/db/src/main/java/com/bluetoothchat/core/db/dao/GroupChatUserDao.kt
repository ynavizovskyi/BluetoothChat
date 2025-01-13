package com.bluetoothchat.core.db.dao

import androidx.room.Dao
import androidx.room.Query
import com.bluetoothchat.core.db.entity.DbGroupChatUser
import kotlinx.coroutines.flow.Flow

@Dao
abstract class GroupChatUserDao : BaseDao<DbGroupChatUser>() {

    @Query("SELECT * FROM GroupChatUser WHERE chatId = :chatId")
    abstract fun observeByChatId(chatId: String): Flow<List<DbGroupChatUser>>

    @Query("SELECT * FROM GroupChatUser WHERE chatId = :chatId")
    abstract fun getByChatId(chatId: String): List<DbGroupChatUser>

    @Query("DELETE FROM GroupChatUser WHERE chatId = :chatId")
    abstract fun deleteAllByChatId(chatId: String): Int

}
