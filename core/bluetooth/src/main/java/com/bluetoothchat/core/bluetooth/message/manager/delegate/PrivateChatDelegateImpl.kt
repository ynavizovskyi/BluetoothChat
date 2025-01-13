package com.bluetoothchat.core.bluetooth.message.manager.delegate

import com.bluetoothchat.core.bluetooth.connection.BtConnectionManager
import com.bluetoothchat.core.bluetooth.message.MessageManager
import com.bluetoothchat.core.bluetooth.message.manager.PrivateChatDelegate
import com.bluetoothchat.core.bluetooth.message.model.Protocol
import com.bluetoothchat.core.bluetooth.message.model.entity.toDomain
import com.bluetoothchat.core.db.datasource.ChatDataSource
import com.bluetoothchat.core.db.datasource.MessageDataSource
import com.bluetoothchat.core.db.datasource.UserDataSource
import com.bluetoothchat.core.dispatcher.ApplicationScope
import com.bluetoothchat.core.dispatcher.DispatcherManager
import com.bluetoothchat.core.domain.model.Chat
import com.bluetoothchat.core.domain.model.Message
import com.bluetoothchat.core.domain.model.MessageContent
import com.bluetoothchat.core.session.SessionImpl
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PrivateChatDelegateImpl @Inject constructor(
    private val chatDataSource: ChatDataSource,
    private val messageDataSource: MessageDataSource,
    private val userDataSource: UserDataSource,
    private val dispatcherManager: DispatcherManager,
    private val applicationScope: ApplicationScope,
    private val session: SessionImpl,
    private val connectionManager: BtConnectionManager,
    private val messageManager: MessageManager,
    private val fileDelegate: FileDelegateImpl,
    private val notificationDelegate: NotificationDelegateImpl,
) : PrivateChatDelegate {

    //TODO: warning! this is highly likely to malfunction
    private val _chatStartedEventChannel: MutableSharedFlow<String> = MutableSharedFlow()
    override val privateChatStartedEventFlow: Flow<String> = _chatStartedEventChannel

    override suspend fun inviteUserToPrivateChat(userAddress: String) {
        withContext(dispatcherManager.io) {
            val connectMessage = messageManager.createPrivateChatInviteToChatRequest()
            sendMessage(message = connectMessage, deviceAddress = userAddress)
        }
    }

    override fun sendPrivateMessage(content: MessageContent, chat: Chat.Private, quotedMessageId: String?) {
        applicationScope.launch(dispatcherManager.io) {
            val myUser = session.getUser()
            val message = createPlainMessage(content = content, quotedMessageId = quotedMessageId)

            messageDataSource.save(chatId = chat.user.deviceAddress, message)

            val response = messageManager.createPrivateChaMessage(message = message, userHash = myUser.hashCode())
            sendMessage(message = response, deviceAddress = chat.user.deviceAddress)
        }
    }

    suspend fun handleMessage(message: Protocol.PrivateChat, deviceAddress: String) {
        when (message) {
            is Protocol.PrivateChat.InviteToChatRequest -> {
                val user = session.getUser()
                val response = messageManager.createPrivateChatInviteToChatResponse(user = user)
                sendMessage(message = response, deviceAddress = deviceAddress)
            }

            is Protocol.PrivateChat.InviteToChatResponse -> {
                val chat = Chat.Private(
                    createdTimestamp = System.currentTimeMillis(),
                    exists = true,
                    user = message.user.toDomain(),
                )
                chatDataSource.save(chat)

                val response = messageManager.createPrivateChatInitiationMessage(user = session.getUser())
                sendMessage(message = response, deviceAddress = deviceAddress)

                fileDelegate.downloadUserPicIfMissing(user = message.user, hostDeviceAddress = deviceAddress)

                _chatStartedEventChannel.emit(deviceAddress)
            }

            is Protocol.PrivateChat.ChatInitiationMessage -> {
                val chat = Chat.Private(
                    createdTimestamp = System.currentTimeMillis(),
                    exists = true,
                    user = message.user.toDomain(),
                )
                chatDataSource.save(chat)

                fileDelegate.downloadUserPicIfMissing(user = message.user, hostDeviceAddress = deviceAddress)

                _chatStartedEventChannel.emit(deviceAddress)
                notificationDelegate.showPrivateChatStartedNotification(chat = chat)
            }

            is Protocol.PrivateChat.ChatMessage -> {
                val chat = chatDataSource.getPrivateChatById(deviceAddress)
//                val user = userDataSource.get(deviceAddress)
                val messageDomain = message.message.toDomain()
                    .copyInstance(timestamp = System.currentTimeMillis(), isReadByMe = false)
                messageDataSource.save(chatId = deviceAddress, messageDomain)

                (messageDomain as? Message.Plain)?.let {
                    if (chat != null) {
                        notificationDelegate.showChatMessageNotification(message = messageDomain, chat = chat, user = chat.user)
                    }
                }

                fileDelegate.downloadContentFile(message = messageDomain, deviceAddress = deviceAddress, chatId = null)

                if (message.userHash != chat?.user?.hashCode()) {
                    val response = messageManager.createPrivateChatUserInfoRequest(userDeviceAddress = deviceAddress)
                    sendMessage(message = response, deviceAddress = deviceAddress)
                }
            }

            is Protocol.PrivateChat.UserInfoRequest -> {
                val user = session.getUser()
                val response = messageManager.createPrivateChatUserInfoResponse(user = user)
                sendMessage(message = response, deviceAddress = deviceAddress)
            }

            is Protocol.PrivateChat.UserInfoResponse -> {
                userDataSource.save(message.user.toDomain())

                fileDelegate.downloadUserPicIfMissing(user = message.user, hostDeviceAddress = deviceAddress)
            }
        }
    }

    private suspend fun sendMessage(message: String, deviceAddress: String) = withContext(dispatcherManager.io) {
        connectionManager.sendMessage(message = message, receiverDevicesAddresses = listOf(deviceAddress))
    }

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
