package com.bluetoothchat.core.db.datasource

import com.bluetoothchat.core.db.DatabaseManager
import com.bluetoothchat.core.db.entity.DbGroupChat
import com.bluetoothchat.core.db.entity.DbGroupChatUser
import com.bluetoothchat.core.db.entity.DbPrivateChat
import com.bluetoothchat.core.db.toDomain
import com.bluetoothchat.core.db.toEntity
import com.bluetoothchat.core.dispatcher.DispatcherManager
import com.bluetoothchat.core.domain.model.Chat
import com.bluetoothchat.core.domain.model.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatDataSource @Inject constructor(
    databaseManager: DatabaseManager,
    private val dispatcherManager: DispatcherManager,
    private val userDataSource: UserDataSource,
    private val messageDataSource: MessageDataSource,
) {

    private val groupChatDao = databaseManager.groupChatDao()
    private val privateChatDao = databaseManager.privateChatDao()
    private val groupChatUserDao = databaseManager.groupChatUserDao()

    suspend fun save(vararg chats: Chat) {
        withContext(dispatcherManager.io) {
            chats.forEach { chat ->
                when (chat) {
                    is Chat.Group -> saveGroupChat(chat)
                    is Chat.Private -> savePrivateChat(chat)
                }
            }
        }
    }

    suspend fun addUserToGroupChat(chatId: String, user: User) {
        withContext(dispatcherManager.io) {
            userDataSource.save(user)
            groupChatUserDao.insert(DbGroupChatUser(chatId = chatId, userDeviceAddress = user.deviceAddress))
        }
    }

    suspend fun deleteUserFromGroupChat(chatId: String, userDeviceAddress: String) {
        withContext(dispatcherManager.io) {
            groupChatUserDao.delete(DbGroupChatUser(chatId = chatId, userDeviceAddress = userDeviceAddress))
        }
    }

    suspend fun deletePrivateChat(userDeviceAddress: String) = withContext(dispatcherManager.io) {
        privateChatDao.delete(userDeviceAddress = userDeviceAddress)
        messageDataSource.deleteAll(chatId = userDeviceAddress)
    }

    suspend fun deleteGroupChat(chatId: String) = withContext(dispatcherManager.io) {
        groupChatDao.delete(id = chatId)
        messageDataSource.deleteAll(chatId = chatId)
    }

    suspend fun setGroupChatDoesNotExist(chatId: String) = withContext(dispatcherManager.io) {
        groupChatDao.setExists(id = chatId, exist = false)
    }

    suspend fun setPrivateChatDoesNotExist(userDeviceAddress: String) = withContext(dispatcherManager.io) {
        privateChatDao.setExists(userDeviceAddress = userDeviceAddress, exist = false)
    }

    fun observeAll(): Flow<List<Chat>> {
        return combine(observeGroupChats(), observePrivateChats()) { groupChats, privateChats ->
            groupChats + privateChats
        }
            .flowOn(dispatcherManager.io)
    }

    fun observeGroupChatById(chatId: String): Flow<Chat.Group?> {
        return groupChatDao.observe(id = chatId)
            .flatMapLatest { chat ->
                chat?.let { observeGroupChatUsers(it) } ?: flow<Chat.Group?> { emit(null) }
            }
    }

    fun observePrivateChatById(chatId: String): Flow<Chat.Private?> {
        return privateChatDao.observe(userDeviceAddress = chatId)
            .flatMapLatest { chat ->
                chat?.let { observePrivateChatUser(it) } ?: flow<Chat.Private?> { emit(null) }
            }
    }

    suspend fun getChatById(chatId: String): Chat? =
        getGroupChatById(chatId = chatId) ?: getPrivateChatById(chatId = chatId)

    suspend fun getGroupChatById(chatId: String): Chat.Group? {
        return withContext(dispatcherManager.io) {
            groupChatDao.get(chatId)?.let { getGroupChatUsers(it) }
        }
    }

    suspend fun getPrivateChatById(chatId: String): Chat.Private? {
        return withContext(dispatcherManager.io) {
            privateChatDao.get(chatId)?.let { getPrivateChatUser(it) }
        }
    }

    suspend fun getAllGroupChats(): List<Chat.Group> {
        return withContext(dispatcherManager.io) {
            groupChatDao.getAll().map { getGroupChatUsers(it) }
        }
    }

    suspend fun getGroupChatByHostDeviceAddress(address: String): List<Chat.Group> {
        return withContext(dispatcherManager.io) {
            groupChatDao.getByHostDeviceAddress(address).map { getGroupChatUsers(it) }
        }
    }

    suspend fun getTotalChatCount(): Int {
        return withContext(dispatcherManager.io) {
            (privateChatDao.getCount() ?: 0) + (groupChatDao.getCount() ?: 0)
        }
    }

    private suspend fun saveGroupChat(chat: Chat.Group) {
        val usersDeviceAddresses = chat.users.map { it.deviceAddress }
        userDataSource.save(*chat.users.toTypedArray())
        groupChatDao.insert(chat.toEntity())
        groupChatUserDao.deleteAllByChatId(chat.id)
        groupChatUserDao.insert(usersDeviceAddresses.map { DbGroupChatUser(chatId = chat.id, userDeviceAddress = it) })
    }

    private suspend fun savePrivateChat(chat: Chat.Private) {
        userDataSource.save(chat.user)
        privateChatDao.insert(chat.toEntity())
    }

    private fun observeGroupChats(): Flow<List<Chat.Group>> {
        return groupChatDao.observeAll().distinctUntilChanged().flatMapLatest { groupChats ->
            //TODO: does not emit if some of the data was corrupted
            val groupChatsFlows = groupChats.map { observeGroupChatUsers(it) }
            if (groupChatsFlows.isNotEmpty()) {
                combine(groupChatsFlows) { it.toList() }
            } else {
                flow { emit(emptyList()) }
            }
        }
            .onStart { emit(emptyList()) }
    }

    private fun observePrivateChats(): Flow<List<Chat.Private>> {
        return privateChatDao.observeAll().distinctUntilChanged().flatMapLatest { privateChats ->
            //TODO: does not emit if some of the data was corrupted
            val privateChatsFlows = privateChats.map { observePrivateChatUser(it) }
            if (privateChatsFlows.isNotEmpty()) {
                combine(privateChatsFlows) { it.toList() }
            } else {
                flow { emit(emptyList()) }
            }
        }
            .onStart { emit(emptyList()) }
    }

    private fun observeGroupChatUsers(chat: DbGroupChat): Flow<Chat.Group> {
        return groupChatUserDao.observeByChatId(chat.id).distinctUntilChanged().flatMapLatest {
            val userFlows = it.map { userDataSource.observe(it.userDeviceAddress).distinctUntilChanged() }
            if (userFlows.isNotEmpty()) {
                combine(userFlows) {
                    val users = it.toList().filterNotNull()
                    chat.toDomain(users)
                }
            } else {
                flow { chat.toDomain(emptyList()) }
            }
        }
    }

    private fun observePrivateChatUser(chat: DbPrivateChat): Flow<Chat.Private> {
        return userDataSource.observe(chat.userDeviceAddress).distinctUntilChanged().map {
            chat.toDomain(user = it!!)
        }
    }

    private suspend fun getGroupChatUsers(chat: DbGroupChat): Chat.Group {
        val users =
            groupChatUserDao.getByChatId(chat.id).map { userDataSource.get(it.userDeviceAddress) }.filterNotNull()
        return chat.toDomain(users)
    }

    private suspend fun getPrivateChatUser(chat: DbPrivateChat): Chat.Private? {
        val user = userDataSource.get(chat.userDeviceAddress)
        return user?.let { chat.toDomain(user) }
    }

}
