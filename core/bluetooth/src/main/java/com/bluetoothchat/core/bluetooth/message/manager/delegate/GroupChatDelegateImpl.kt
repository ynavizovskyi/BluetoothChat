package com.bluetoothchat.core.bluetooth.message.manager.delegate

import android.util.Log
import com.bluetoothchat.core.bluetooth.connection.BtConnectionManager
import com.bluetoothchat.core.bluetooth.message.MessageManager
import com.bluetoothchat.core.bluetooth.message.manager.GroupChatDelegate
import com.bluetoothchat.core.bluetooth.message.manager.GroupChatHistorySyncLimit
import com.bluetoothchat.core.bluetooth.message.model.Protocol
import com.bluetoothchat.core.bluetooth.message.model.entity.BtFileType
import com.bluetoothchat.core.bluetooth.message.model.entity.BtGroupChat
import com.bluetoothchat.core.bluetooth.message.model.entity.BtMessage
import com.bluetoothchat.core.bluetooth.message.model.entity.toDomain
import com.bluetoothchat.core.db.datasource.ChatDataSource
import com.bluetoothchat.core.db.datasource.MessageDataSource
import com.bluetoothchat.core.db.datasource.UserDataSource
import com.bluetoothchat.core.dispatcher.ApplicationScope
import com.bluetoothchat.core.dispatcher.DispatcherManager
import com.bluetoothchat.core.domain.model.Chat
import com.bluetoothchat.core.domain.model.GroupUpdateType
import com.bluetoothchat.core.domain.model.Message
import com.bluetoothchat.core.domain.model.MessageContent
import com.bluetoothchat.core.domain.model.Picture
import com.bluetoothchat.core.domain.model.primary
import com.bluetoothchat.core.filemanager.file.FileState
import com.bluetoothchat.core.session.SessionImpl
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GroupChatDelegateImpl @Inject constructor(
    private val dispatcherManager: DispatcherManager,
    private val applicationScope: ApplicationScope,
    private val connectionManager: BtConnectionManager,
    private val chatDataSource: ChatDataSource,
    private val messageDataSource: MessageDataSource,
    private val userDataSource: UserDataSource,
    private val session: SessionImpl,
    private val messageManager: MessageManager,
    private val fileDelegate: FileDelegateImpl,
    private val notificationDelegate: NotificationDelegateImpl,
) : GroupChatDelegate {

    //TODO: warning! this is highly likely to malfunction
    private val _addedToGroupChatEventFlow: MutableSharedFlow<String> = MutableSharedFlow()
    override val addedToGroupChatEventFlow: Flow<String> = _addedToGroupChatEventFlow

    override suspend fun createGroupChat(name: String, picture: Picture?): String = withContext(dispatcherManager.io) {
        val user = session.getUser()
        val chatId = UUID.randomUUID().toString()

        val chat = Chat.Group(
            id = chatId,
            createdTimestamp = System.currentTimeMillis(),
            exists = true,
            hostDeviceAddress = user.deviceAddress,
            name = name,
            picture = picture,
            users = listOf(user),
        )
        chatDataSource.save(chat)

        //No distribution needed as there are no members at this point
        val message = createGroupUpdateMessage(
            updateType = GroupUpdateType.GROUP_CREATED,
            targetDeviceAddress = null,
        )
        messageDataSource.save(chatId = chatId, message)

        chatId
    }

    override suspend fun addUserToChat(chatId: String, userAddress: String) {
        withContext(dispatcherManager.io) {
            val message = messageManager.createGroupChatInviteToChatRequest(chatId = chatId)
            sendMessage(message = message, deviceAddresses = listOf(userAddress))
        }
    }

    override suspend fun removeUserFromChat(chatId: String, updateType: GroupUpdateType, userAddress: String) =
        withContext(dispatcherManager.io) {
            val chat = chatDataSource.getGroupChatById(chatId) ?: return@withContext

            //Mapping users before removing user from the group so they also get the update
            val addresses = chat.users.filter { !session.isCurrentUser(it) }.map { it.deviceAddress }

            val message = createGroupUpdateMessage(
                updateType = updateType,
                targetDeviceAddress = userAddress,
            )
            messageDataSource.save(chatId = chatId, message)

            chatDataSource.deleteUserFromGroupChat(chatId = chat.id, userDeviceAddress = userAddress)

            val updatedChat = chatDataSource.getGroupChatById(chat.id) ?: return@withContext
            val response = messageManager.createGroupChatHostMessage(
                chatId = updatedChat.id,
                chatHash = updatedChat.hashCode(),
                message = message,
            )
            sendMessage(message = response, deviceAddresses = addresses)
        }

    override suspend fun leaveChat(chatId: String) = withContext(dispatcherManager.io) {
        val chat = chatDataSource.getGroupChatById(chatId) ?: return@withContext

        val response = messageManager.createLeaveChatRequestMessage(chatId = chat.id)
        sendMessage(message = response, deviceAddresses = listOf(chat.hostDeviceAddress))
    }

    override suspend fun downloadMessageFile(content: MessageContent.File, chatId: String) =
        withContext(dispatcherManager.io) {
            val chat = chatDataSource.getGroupChatById(chatId) ?: return@withContext

            fileDelegate.downloadContentFile(content = content, deviceAddress = chat.hostDeviceAddress, chatId = chatId)
        }

    override fun sendGroupChatMessage(chatId: String, content: MessageContent, quotedMessageId: String?) {
        applicationScope.launch(dispatcherManager.io) {
            val chat = chatDataSource.getGroupChatById(chatId = chatId) ?: return@launch
            val message = createPlainMessage(content = content, quotedMessageId = quotedMessageId)
            messageDataSource.save(chatId = chatId, message)

            //If device is host send message to everyone, otherwise to host to distribute
            val isHost = session.isCurrentUser(deviceAddress = chat.hostDeviceAddress)
            if (isHost) {
                val addresses = chat.users.filter { !session.isCurrentUser(it) }.map { it.deviceAddress }
                val response = messageManager.createGroupChatHostMessage(
                    chatId = chat.id,
                    chatHash = chat.hashCode(),
                    message = message,
                )
                sendMessage(message = response, deviceAddresses = addresses)

                (message as? Message.Plain)?.content?.filterIsInstance<MessageContent.File>()?.firstOrNull()?.let {
                    applicationScope.launch(dispatcherManager.io) {
                        val response = messageManager.createGroupChatFileReadyMessage(
                            fileType = BtFileType.CHAT_FILE,
                            chatId = chat.id,
                            fileName = it.fileName
                        )
                        sendMessage(message = response, deviceAddresses = addresses)
                    }
                }
            } else {
                val currentUser = session.getUser()
                val response = messageManager.createGroupChatClientMessage(
                    chatId = chat.id,
                    userHash = currentUser.hashCode(),
                    message = message,
                )
                sendMessage(message = response, deviceAddresses = listOf(chat.hostDeviceAddress))
            }
        }
    }

    suspend fun onUserLeft(chatId: String, userAddress: String, excludeFromUpdateMessage: Boolean) =
        withContext(dispatcherManager.io) {
            val chat = chatDataSource.getGroupChatById(chatId) ?: return@withContext

            val addresses = chat.users.filter {
                !session.isCurrentUser(it) && if (excludeFromUpdateMessage) it.deviceAddress != userAddress else true
            }.map { it.deviceAddress }

            val message = createGroupUpdateMessage(
                updateType = GroupUpdateType.USER_LEFT,
                targetDeviceAddress = userAddress,
            )
            messageDataSource.save(chatId = chatId, message)

            chatDataSource.deleteUserFromGroupChat(chatId = chat.id, userDeviceAddress = userAddress)

            val updatedChat = chatDataSource.getGroupChatById(chat.id) ?: return@withContext
            val response = messageManager.createGroupChatHostMessage(
                chatId = updatedChat.id,
                chatHash = updatedChat.hashCode(),
                message = message,
            )
            sendMessage(message = response, deviceAddresses = addresses)
        }

    suspend fun onChatUpdated(
        hostTimestamp: Long,
        chatId: String,
        //Could be null if this is an update message and the group info has not changed
        chat: BtGroupChat?,
        messages: List<BtMessage>
    ) {
        val hostTieDifferenceMillis = hostTimestamp - System.currentTimeMillis()
        val domainMessages = messages.map {
            it.toDomain().copyInstance(
                timestamp = it.timestamp - hostTieDifferenceMillis,
                isReadByMe = false,
            )
        }.toTypedArray()
        messageDataSource.save(chatId = chatId, *domainMessages)

        if (chat != null) {
            chatDataSource.save(chat.toDomain())

            fileDelegate.downloadChatPicIfMissing(chat = chat)
            chat.users.forEach { user ->
                fileDelegate.downloadUserPicIfMissing(user = user, hostDeviceAddress = chat.hostDeviceAddress)
            }
        }
    }

    suspend fun handleMessage(message: Protocol.GroupChat, deviceAddress: String) {
        when (message) {
            is Protocol.GroupChat.InviteToChatRequest -> {
                val user = session.getUser()
                val lastMessageId = messageDataSource.getLast(chatId = message.chatId)?.id
                val response = messageManager.createGroupChatInviteToChatResponse(
                    chatId = message.chatId,
                    user = user,
                    lastMessageId = lastMessageId,
                )
                sendMessage(message = response, deviceAddresses = listOf(deviceAddress))
            }

            is Protocol.GroupChat.InviteToChatResponse -> {
                chatDataSource.addUserToGroupChat(chatId = message.chatId, user = message.user.toDomain())
                val chat = chatDataSource.getGroupChatById(message.chatId) ?: return

                val userAddedMessageAddresses = chat.users.filter {
                    //The new user will get this message with the initiation batch
                    !session.isCurrentUser(it) && it.deviceAddress != deviceAddress
                }.map { it.deviceAddress }
                val userAddedMessage = createGroupUpdateMessage(
                    updateType = GroupUpdateType.USER_ADDED,
                    targetDeviceAddress = message.user.deviceAddress,
                )
                messageDataSource.save(chatId = message.chatId, userAddedMessage)

                val userAddedResponse = messageManager.createGroupChatHostMessage(
                    chatId = chat.id,
                    chatHash = chat.hashCode(),
                    message = userAddedMessage,
                )
                sendMessage(message = userAddedResponse, deviceAddresses = userAddedMessageAddresses)

                val messageHistory = if (message.lastMessageId != null) {
                    messageDataSource.getAllNewerThan(
                        chatId = message.chatId,
                        messageId = message.lastMessageId,
                        limit = GroupChatHistorySyncLimit,
                    )
                } else {
                    messageDataSource.getAll(chatId = message.chatId, limit = GroupChatHistorySyncLimit)
                }

                val response = messageManager.createGroupChatInitiationMessage(chat = chat, messages = messageHistory)
                sendMessage(message = response, deviceAddresses = listOf(deviceAddress))
            }

            is Protocol.GroupChat.ChatInitiationMessage -> {
                onChatUpdated(
                    hostTimestamp = message.hostTimestamp,
                    chatId = message.chat.id,
                    chat = message.chat,
                    messages = message.messages,
                )

                notificationDelegate.showAddedToGroupChatNotification(chat = message.chat.toDomain())
                _addedToGroupChatEventFlow.emit(message.chat.id)
            }

            is Protocol.GroupChat.HostChatMessage -> {
                val hostTieDifferenceMillis = message.hostTimestamp - System.currentTimeMillis()

                val chat = chatDataSource.getGroupChatById(message.chatId) ?: return
                val user = userDataSource.get(message.message.userDeviceAddress)

                val timestamp = message.message.timestamp - hostTieDifferenceMillis
                val messageDomain = message.message.toDomain().copyInstance(timestamp = timestamp, isReadByMe = false)
                messageDataSource.save(chatId = message.chatId, messageDomain)

                (messageDomain as? Message.Plain)?.let {
                    notificationDelegate.showChatMessageNotification(message = messageDomain, chat = chat, user = user)
                }

                if (message.chatHash != chat.hashCode()) {
                    val request = messageManager.createGroupChatInfoRequest(chatId = message.chatId)
                    sendMessage(message = request, deviceAddresses = listOf(deviceAddress))
                }
            }

            is Protocol.GroupChat.ClientChatMessage -> {
                val chat = chatDataSource.getGroupChatById(message.chatId) ?: return
                val user = userDataSource.get(message.message.userDeviceAddress)

                val isUserGroupMember = chat.users.any { it.deviceAddress == deviceAddress }

                if (isUserGroupMember) {
                    val messageDomain = message.message.toDomain()
                        .copyInstance(timestamp = System.currentTimeMillis(), isReadByMe = false)
                    messageDataSource.save(chatId = message.chatId, messageDomain)

                    (messageDomain as? Message.Plain)?.let {
                        notificationDelegate.showChatMessageNotification(
                            message = messageDomain,
                            chat = chat,
                            user = user,
                        )
                    }

                    val user = chat.users.firstOrNull { it.deviceAddress == deviceAddress }
                    if (message.userHash != user.hashCode()) {
                        val request = messageManager.createGroupChatUserInfoRequest(address = deviceAddress)
                        sendMessage(message = request, deviceAddresses = listOf(deviceAddress))
                    }

                    //Distribute message to clients, excluding the sender
                    val clientAddresses = chat.users.filter {
                        !session.isCurrentUser(it) && it.deviceAddress != deviceAddress
                    }.map { it.deviceAddress }

                    val response = messageManager.createGroupChatHostMessage(
                        chatId = chat.id,
                        chatHash = chat.hashCode(),
                        message = messageDomain,
                    )
                    sendMessage(message = response, deviceAddresses = clientAddresses)


                    //TODO: should probably not be here
                    (messageDomain as? Message.Plain)?.content?.primary()?.let { content ->
                        if (content is MessageContent.File) {
                            fileDelegate.downloadContentFile(
                                content = content,
                                deviceAddress = deviceAddress,
                                chatId = chat.id
                            )

                            //Schedule content ready message
                            scheduleFileReadyMessage(
                                chatId = message.chatId,
                                fileName = content.fileName,
                                filSizeBytes = content.fileSizeBytes,
                                //Filtering out the sender so they don't download their own file
                                clientAddresses = clientAddresses.filterNot { it == deviceAddress }
                            )
                        }
                    }
                } else {
                    val response = messageManager.createGroupChatInfoResponse(chat = chat)
                    sendMessage(message = response, deviceAddresses = listOf(deviceAddress))
                }
            }

            is Protocol.GroupChat.ChatInfoRequest -> {
                val chat = chatDataSource.getGroupChatById(message.chatId) ?: return
                val response = messageManager.createGroupChatInfoResponse(chat = chat)
                sendMessage(message = response, deviceAddresses = listOf(deviceAddress))
            }

            is Protocol.GroupChat.ChatInfoResponse -> {
                chatDataSource.save(message.chat.toDomain())

                fileDelegate.downloadChatPicIfMissing(chat = message.chat)

                applicationScope.launch(dispatcherManager.io) {
                    //Giving the host some time to download missing user pics before clients attemp to request them
                    delay(3000)
                    message.chat.users.forEach { user ->
                        fileDelegate.downloadUserPicIfMissing(
                            user = user,
                            hostDeviceAddress = message.chat.hostDeviceAddress,
                        )
                    }
                }
            }

            is Protocol.GroupChat.UserInfoRequest -> {
                val response = messageManager.createGroupChatUserInfoResponse(user = session.getUser())
                sendMessage(message = response, deviceAddresses = listOf(deviceAddress))
            }

            is Protocol.GroupChat.UserInfoResponse -> {
                userDataSource.save(message.user.toDomain())
                fileDelegate.downloadUserPicIfMissing(
                    user = message.user,
                    hostDeviceAddress = message.user.deviceAddress,
                )
            }

            is Protocol.GroupChat.FileReady -> {
                val response = messageManager.createFileRequest(
                    fileType = message.fileType,
                    chatId = message.chatId,
                    fileName = message.fileName,
                )
                sendMessage(message = response, deviceAddresses = listOf(deviceAddress))
            }

            is Protocol.GroupChat.LeaveChatReqeust -> {
                removeUserFromChat(
                    chatId = message.chatId,
                    updateType = GroupUpdateType.USER_LEFT,
                    userAddress = deviceAddress,
                )
            }
        }
    }

    private suspend fun sendMessage(message: String, deviceAddresses: List<String>) {
        withContext(dispatcherManager.io) {
            connectionManager.sendMessage(message = message, receiverDevicesAddresses = deviceAddresses)
        }
    }

    private fun scheduleFileReadyMessage(
        chatId: String,
        fileName: String,
        filSizeBytes: Long,
        clientAddresses: List<String>,
    ) {
        //DOes not always work
        applicationScope.launch(dispatcherManager.io) {
            fileDelegate.observeChatFileState(chatId = chatId, fileName = fileName, fileSizeBytes = filSizeBytes)
                .onEach { kk ->
                    Log.v("CommunicationManager", "File downloaded event onEach $kk")
                }
                .first { it is FileState.Downloaded }

            val response = messageManager.createGroupChatFileReadyMessage(
                fileType = BtFileType.CHAT_FILE,
                chatId = chatId,
                fileName = fileName
            )
            sendMessage(message = response, deviceAddresses = clientAddresses)
        }
    }

    private suspend fun createGroupUpdateMessage(updateType: GroupUpdateType, targetDeviceAddress: String?) =
        Message.GroupUpdate(
            id = UUID.randomUUID().toString(),
            userDeviceAddress = session.getUser().deviceAddress,
            timestamp = System.currentTimeMillis(),
            isReadByMe = true,
            updateType = updateType,
            targetDeviceAddress = targetDeviceAddress,
        )

    private suspend fun createPlainMessage(content: MessageContent, quotedMessageId: String?) =
        Message.Plain(
            id = UUID.randomUUID().toString(),
            userDeviceAddress = session.getUser().deviceAddress,
            timestamp = System.currentTimeMillis(),
            isReadByMe = true,
            quotedMessageId = quotedMessageId,
            content = listOf(content),
        )

}
