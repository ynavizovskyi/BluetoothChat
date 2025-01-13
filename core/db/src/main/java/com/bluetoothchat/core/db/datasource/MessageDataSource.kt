package com.bluetoothchat.core.db.datasource

import com.bluetoothchat.core.db.DatabaseManager
import com.bluetoothchat.core.db.toDomain
import com.bluetoothchat.core.db.toEntity
import com.bluetoothchat.core.dispatcher.DispatcherManager
import com.bluetoothchat.core.domain.model.Message
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MessageDataSource @Inject constructor(
    databaseManager: DatabaseManager,
    private val dispatcherManager: DispatcherManager,
) {

    private val dao = databaseManager.messageDao()

    suspend fun save(chatId: String, vararg data: Message) = withContext(dispatcherManager.io) {
        dao.insert(data.map { it.toEntity(chatId) })
    }

    suspend fun get(messageId: String): Message? = withContext(dispatcherManager.io) {
        dao.get(messageId)?.toDomain()
    }

    suspend fun getAll(chatId: String, limit: Int): List<Message> = withContext(dispatcherManager.io) {
        dao.getAll(chatId = chatId, count = limit).map { it.toDomain() }
    }

    suspend fun getAllNewerThan(chatId: String, messageId: String, limit: Int): List<Message> =
        withContext(dispatcherManager.io) {
            val message = dao.get(messageId)
            if (message != null) {
                dao.getAllNewerThan(chatId = chatId, timestamp = message.timestamp, count = limit).map { it.toDomain() }
            } else {
                emptyList()
            }
        }

    suspend fun getOlderThan(chatId: String, messageId: String, count: Int): List<Message> =
        withContext(dispatcherManager.io) {
            val message = dao.get(messageId)
            if (message != null) {
                dao.getOlderThan(chatId = chatId, timestamp = message.timestamp, count = count).map { it.toDomain() }
            } else {
                emptyList()
            }
        }

    suspend fun getNewest(chatId: String, count: Int): List<Message> =
        withContext(dispatcherManager.io) {
            dao.getNewest(chatId = chatId, count = count).map { it.toDomain() }
        }

    suspend fun observeAllStartingWithThan(chatId: String, messageId: String?) = flow {
        val startMessage = messageId?.let { dao.get(messageId) }
        emit(startMessage)
    }
        .flatMapLatest { startMessage ->
            dao.observeAllStartingWith(chatId = chatId, timestamp = startMessage?.timestamp ?: 0)
        }
        .distinctUntilChanged()
        .map { messages ->
            messages.map { it.toDomain() }
        }
        .flowOn(dispatcherManager.io)


    suspend fun getLast(chatId: String): Message? = withContext(dispatcherManager.io) {
        dao.getLast(chatId)?.toDomain()
    }

    suspend fun deleteAll(chatId: String) = withContext(dispatcherManager.io) {
        dao.deleteAll(chatId)
    }

    fun observeLast(chatId: String): Flow<Message?> = dao.observeLast(chatId)
        .map { it?.toDomain() }
        .flowOn(dispatcherManager.io)

    fun observeAll(chatId: String): Flow<List<Message>> = dao.observeAll(chatId)
        .distinctUntilChanged()
        .map { messages ->
            messages.map { it.toDomain() }
        }
        .flowOn(dispatcherManager.io)

    suspend fun getCount(chatId: String): Int = dao.getCount(chatId = chatId)

    fun observeUnreadCount(chatId: String): Flow<Int> =
        dao.observeUnreadCount(chatId = chatId).map { it ?: 2 }.flowOn(dispatcherManager.io)

    suspend fun markAllAsRead(chatId: String) =
        withContext(dispatcherManager.io) { dao.setAllAsRead(chatId = chatId, isReadByMe = true) }

    suspend fun updateUserDeviceAddress(oldAddress: String, newAddress: String) = withContext(dispatcherManager.io) {
        dao.updateUserDeviceAddress(oldAddress = oldAddress, newAddress = newAddress)
    }

}
