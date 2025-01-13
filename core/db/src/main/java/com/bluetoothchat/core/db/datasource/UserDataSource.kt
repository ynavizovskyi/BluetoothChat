package com.bluetoothchat.core.db.datasource

import com.bluetoothchat.core.db.DatabaseManager
import com.bluetoothchat.core.db.toDomain
import com.bluetoothchat.core.db.toEntity
import com.bluetoothchat.core.dispatcher.DispatcherManager
import com.bluetoothchat.core.domain.model.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserDataSource @Inject constructor(
    private val dispatcherManager: DispatcherManager,
    private val databaseManager: DatabaseManager,
) {

    private val dao = databaseManager.userDao()

    suspend fun save(vararg data: User) = withContext(dispatcherManager.io) { dao.insert(data.map { it.toEntity() }) }

    fun observe(deviceAddress: String): Flow<User?> =
        dao.observe(deviceAddress).map { it?.toDomain() }.distinctUntilChanged().flowOn(dispatcherManager.io)

    fun observeAll(): Flow<List<User>> = dao.observeAll().map { users ->
        users.map { it.toDomain() }
    }.flowOn(dispatcherManager.io)

    suspend fun get(deviceAddress: String): User? =
        withContext(dispatcherManager.io) { dao.get(deviceAddress)?.toDomain() }

    suspend fun delete(user: User) = withContext(dispatcherManager.io) { dao.delete(user.toEntity()) }

}
