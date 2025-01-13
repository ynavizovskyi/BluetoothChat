package com.bluetoothchat.core.db.dao

import androidx.room.Dao
import androidx.room.Query
import com.bluetoothchat.core.db.entity.DbUser
import kotlinx.coroutines.flow.Flow

@Dao
abstract class UserDao : BaseDao<DbUser>() {

    @Query("SELECT * FROM User ORDER BY deviceAddress DESC")
    abstract fun observeAll(): Flow<List<DbUser>>

    @Query("SELECT * FROM User WHERE deviceAddress = :deviceAddress")
    abstract fun observe(deviceAddress: String): Flow<DbUser?>

    @Query("SELECT * FROM User WHERE deviceAddress = :deviceAddress")
    abstract fun get(deviceAddress: String): DbUser?

}
