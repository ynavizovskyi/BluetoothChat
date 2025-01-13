package com.bluetoothchat.core.db.dao

import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Update

abstract class BaseDao<E> {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insert(entity: E)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insert(entities: List<E>)

    @Update
    abstract suspend fun update(entity: E)

    @Update
    abstract suspend fun update(entities: List<E>)

    @Delete
    abstract suspend fun delete(entities: List<E>)

    @Delete
    abstract suspend fun delete(entitiy: E)

}
