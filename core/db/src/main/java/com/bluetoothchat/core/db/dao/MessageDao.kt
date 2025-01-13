package com.bluetoothchat.core.db.dao

import androidx.room.Dao
import androidx.room.Query
import com.bluetoothchat.core.db.entity.DbMessage
import kotlinx.coroutines.flow.Flow

@Dao
abstract class MessageDao : BaseDao<DbMessage>() {

    @Query("SELECT * FROM Message WHERE chatId = :chatId ORDER BY timestamp DESC")
    abstract fun observeAll(chatId: String): Flow<List<DbMessage>>

    @Query("SELECT * FROM Message WHERE chatId = :chatId AND id IN (SELECT id FROM Message WHERE chatId = :chatId ORDER BY timestamp DESC LIMIT :count) ORDER BY timestamp DESC")
    abstract fun getAll(chatId: String, count: Int): List<DbMessage>

    @Query("SELECT * FROM Message WHERE id = :messageId")
    abstract fun get(messageId: String): DbMessage?

    @Query("SELECT * FROM Message WHERE chatId = :chatId AND id IN (SELECT id FROM Message WHERE chatId = :chatId AND timestamp > :timestamp ORDER BY timestamp DESC LIMIT :count) ORDER BY timestamp DESC")
    abstract fun getAllNewerThan(chatId: String, timestamp: Long, count: Int): List<DbMessage>

    @Query("SELECT * FROM Message WHERE chatId = :chatId AND timestamp < :timestamp ORDER BY timestamp DESC LIMIT :count")
    abstract fun getOlderThan(chatId: String, timestamp: Long, count: Int): List<DbMessage>

    @Query("SELECT * FROM Message WHERE chatId = :chatId AND id IN (SELECT id FROM Message WHERE chatId = :chatId ORDER BY timestamp DESC LIMIT :count) ORDER BY timestamp DESC")
    abstract fun getNewest(chatId: String, count: Int): List<DbMessage>

    @Query("SELECT * FROM Message WHERE chatId = :chatId AND timestamp >= :timestamp ORDER BY timestamp DESC")
    abstract fun observeAllStartingWith(chatId: String, timestamp: Long): Flow<List<DbMessage>>

    @Query("SELECT * FROM Message WHERE timestamp IS (SELECT MAX(timestamp) FROM Message WHERE chatId = :chatId)")
    abstract fun getLast(chatId: String): DbMessage?

    @Query("SELECT * FROM Message WHERE timestamp IS (SELECT MAX(timestamp) FROM Message WHERE chatId = :chatId)")
    abstract fun observeLast(chatId: String): Flow<DbMessage?>

    @Query("SELECT COUNT(id) FROM Message WHERE chatId = :chatId")
    abstract fun getCount(chatId: String): Int //TODO: should be nullable?

    @Query("SELECT COUNT(id) FROM Message WHERE chatId = :chatId AND NOT isReadByMe")
    abstract fun observeUnreadCount(chatId: String): Flow<Int?>

    @Query("UPDATE Message SET isReadByMe = :isReadByMe WHERE chatId = :chatId AND NOT isReadByMe")
    abstract fun setAllAsRead(chatId: String, isReadByMe: Boolean)

    @Query("UPDATE Message SET userDeviceAddress = :newAddress WHERE userDeviceAddress = :oldAddress")
    abstract fun updateUserDeviceAddress(oldAddress: String, newAddress: String)

    @Query("DELETE FROM Message WHERE chatId = :chatId")
    abstract fun deleteAll(chatId: String)

}
