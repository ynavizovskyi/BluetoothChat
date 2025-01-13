package com.bluetoothchat.core.db.dao

import androidx.room.Dao
import androidx.room.Query
import com.bluetoothchat.core.db.entity.DbPrivateChat
import kotlinx.coroutines.flow.Flow

@Dao
abstract class PrivateChatDao : BaseDao<DbPrivateChat>() {

    @Query("SELECT * FROM PrivateChat")
    abstract fun observeAll(): Flow<List<DbPrivateChat>>

    @Query("SELECT * FROM PrivateChat WHERE userDeviceAddress = :userDeviceAddress")
    abstract fun observe(userDeviceAddress: String): Flow<DbPrivateChat?>

    @Query("SELECT * FROM PrivateChat WHERE userDeviceAddress = :userDeviceAddress")
    abstract fun get(userDeviceAddress: String): DbPrivateChat?

    @Query("DELETE FROM PrivateChat WHERE userDeviceAddress = :userDeviceAddress")
    abstract fun delete(userDeviceAddress: String)

    @Query("UPDATE PrivateChat SET exist = :exist WHERE userDeviceAddress = :userDeviceAddress")
    abstract fun setExists(userDeviceAddress: String, exist: Boolean)

    @Query("SELECT COUNT(userDeviceAddress) FROM PrivateChat")
    abstract fun getCount(): Int?

}
