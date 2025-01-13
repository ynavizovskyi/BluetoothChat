package com.bluetoothchat.core.bluetooth.message.manager

import com.bluetoothchat.core.bluetooth.message.ConnectionState
import com.bluetoothchat.core.domain.model.Chat
import com.bluetoothchat.core.domain.model.GroupUpdateType
import com.bluetoothchat.core.domain.model.Message
import com.bluetoothchat.core.domain.model.MessageContent
import com.bluetoothchat.core.domain.model.Picture
import com.bluetoothchat.core.domain.model.User
import com.bluetoothchat.core.filemanager.file.FileState
import kotlinx.coroutines.flow.Flow

const val EstablishConnectionTimeoutMillis = 20_000L
internal const val GroupChatHistorySyncLimit = 300

interface ConnectionDelegate {
    suspend fun isBluetoothEnabled(): Boolean
    suspend fun listenForConnectionRequests()
    suspend fun connect(deviceAddress: String): Boolean
    suspend fun disconnect(deviceAddress: String)
    suspend fun disconnectAll()
}

interface FileDelegate {
    fun observeChatFilesBeingDownloaded(chatId: String): Flow<List<FileState.Downloading>>
    fun observeUserFilesBeingDownloaded(): Flow<List<FileState.Downloading>>
    fun observeChatFileState(chatId: String, fileName: String, fileSizeBytes: Long): Flow<FileState>
}

interface NotificationDelegate {
    fun onChatScreenOpened(chatId: String)
    fun onChatScreenClosed(chatId: String)
    fun onConnectScreenOpen()
    fun onConnectScreenClosed()
    suspend fun showChatMessageNotification(chat: Chat, message: Message.Plain, user: User?)
    suspend fun showPrivateChatStartedNotification(chat: Chat.Private)
    suspend fun showAddedToGroupChatNotification(chat: Chat.Group)
}

interface PrivateChatDelegate {
    val privateChatStartedEventFlow: Flow<String>

    suspend fun inviteUserToPrivateChat(userAddress: String)
    fun sendPrivateMessage(content: MessageContent, chat: Chat.Private, quotedMessageId: String?)
}

interface GroupChatDelegate {
    val addedToGroupChatEventFlow: Flow<String>

    //Returns id of the created group chat
    suspend fun createGroupChat(name: String, picture: Picture?): String
    suspend fun addUserToChat(chatId: String, userAddress: String)
    suspend fun removeUserFromChat(chatId: String, updateType: GroupUpdateType, userAddress: String)
    suspend fun leaveChat(chatId: String)
    suspend fun downloadMessageFile(content: MessageContent.File, chatId: String)
    fun sendGroupChatMessage(chatId: String, content: MessageContent, quotedMessageId: String?)
}

interface CommunicationManager : ConnectionDelegate, PrivateChatDelegate, GroupChatDelegate {
    val connectedDevicesFlow: Flow<List<String>>
    val connectionStateFlow: Flow<Map<String, ConnectionState>>

    suspend fun ensureStarted()
}
