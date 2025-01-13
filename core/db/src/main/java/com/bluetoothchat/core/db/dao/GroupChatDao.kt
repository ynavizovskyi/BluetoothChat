package com.bluetoothchat.core.db.dao

import androidx.room.Dao
import androidx.room.Query
import com.bluetoothchat.core.db.entity.DbGroupChat
import kotlinx.coroutines.flow.Flow

@Dao
abstract class GroupChatDao : BaseDao<DbGroupChat>() {

    @Query("SELECT * FROM GroupChat")
    abstract fun observeAll(): Flow<List<DbGroupChat>>

    @Query("SELECT * FROM GroupChat")
    abstract fun getAll(): List<DbGroupChat>

    @Query("SELECT * FROM GroupChat WHERE id = :id")
    abstract fun observe(id: String): Flow<DbGroupChat?>

    @Query("SELECT * FROM GroupChat WHERE id = :id")
    abstract fun get(id: String): DbGroupChat?

    @Query("SELECT * FROM GroupChat WHERE hostDeviceAddress = :address")
    abstract fun getByHostDeviceAddress(address: String): List<DbGroupChat>

    @Query("DELETE FROM GroupChat WHERE id = :id")
    abstract fun delete(id: String)

    @Query("UPDATE GroupChat SET exist = :exist WHERE id = :id")
    abstract fun setExists(id: String, exist: Boolean)

    @Query("SELECT COUNT(id) FROM GroupChat")
    abstract fun getCount(): Int?

}
