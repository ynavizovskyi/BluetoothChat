package com.bluetoothchat.core.bluetooth.message.manager.delegate

import android.util.Log
import com.bluetoothchat.core.bluetooth.connection.BtConnectionManager
import com.bluetoothchat.core.bluetooth.message.MessageManager
import com.bluetoothchat.core.bluetooth.message.manager.ConnectionDelegate
import com.bluetoothchat.core.bluetooth.message.manager.GroupChatHistorySyncLimit
import com.bluetoothchat.core.bluetooth.message.model.BtParseMessageResult
import com.bluetoothchat.core.bluetooth.message.model.Protocol
import com.bluetoothchat.core.bluetooth.message.model.entity.BtGroupChatClientInfo
import com.bluetoothchat.core.bluetooth.message.model.entity.BtGroupChatHashInfo
import com.bluetoothchat.core.bluetooth.message.model.entity.BtGroupChatInfo
import com.bluetoothchat.core.bluetooth.message.model.entity.toBluetooth
import com.bluetoothchat.core.bluetooth.message.model.entity.toDomain
import com.bluetoothchat.core.db.datasource.ChatDataSource
import com.bluetoothchat.core.db.datasource.MessageDataSource
import com.bluetoothchat.core.db.datasource.UserDataSource
import com.bluetoothchat.core.dispatcher.ApplicationScope
import com.bluetoothchat.core.dispatcher.DispatcherManager
import com.bluetoothchat.core.prefs.USER_DEVICE_ADDRESS_NOT_SET
import com.bluetoothchat.core.session.SessionImpl
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ConnectionDelegateImpl @Inject constructor(
    private val dispatcherManager: DispatcherManager,
    private val applicationScope: ApplicationScope,
    private val groupChatManager: GroupChatDelegateImpl,
    private val connectionManager: BtConnectionManager,
    private val chatDataSource: ChatDataSource,
    private val messageDataSource: MessageDataSource,
    private val userDataSource: UserDataSource,
    private val session: SessionImpl,
    private val messageManager: MessageManager,
    private val fileDelegate: FileDelegateImpl,
) : ConnectionDelegate {

    private var started = false

    val connectingDevicesFlow: MutableStateFlow<Set<String>> = MutableStateFlow(emptySet())

    override suspend fun isBluetoothEnabled(): Boolean = connectionManager.isBluetoothEnabled()

    override suspend fun listenForConnectionRequests() {
        if (!started) {
            started = true
            applicationScope.launch(dispatcherManager.io) {
                connectionManager.listenForConnectionRequests { deviceAddress ->
                    connectingDevicesFlow.value =
                        connectingDevicesFlow.value.toMutableSet().apply { add(deviceAddress) }
                    initConnection(deviceAddress = deviceAddress)
                }
            }
        }
    }

    override suspend fun connect(deviceAddress: String): Boolean = withContext(dispatcherManager.io) {
        connectingDevicesFlow.value = connectingDevicesFlow.value.toMutableSet().apply { add(deviceAddress) }
        val connected = connectionManager.connectToAsClient(deviceAddress = deviceAddress)
        if (connected) {
            initConnection(deviceAddress = deviceAddress)
        } else {
            connectingDevicesFlow.value = connectingDevicesFlow.value.toMutableSet().apply { remove(deviceAddress) }
        }

        return@withContext connected
    }

    override suspend fun disconnect(deviceAddress: String) {
        connectionManager.disconnect(deviceAddress)
        connectingDevicesFlow.value = connectingDevicesFlow.value.toMutableSet().apply { remove(deviceAddress) }
    }

    override suspend fun disconnectAll() {
        connectionManager.disconnectAll()
        connectingDevicesFlow.value = emptySet()
    }

    suspend fun handleIncompatibleProtocolsError(
        error: BtParseMessageResult.Error.IncompatibleProtocols,
        deviceAddress: String,
    ) {
        Log.v("ConnectionDelegateImpl", "handleIncompatibleProtocolsError: $error")
        //Make sure the message is sent before disconnecting
        delay(300)
        disconnect(deviceAddress = deviceAddress)
    }

    suspend fun handleMessage(message: Protocol.InitConnection, deviceAddress: String) {
        when (message) {
            is Protocol.InitConnection.Request -> {
                initMyUserDeviceAddress(deviceAddress = message.receiverDeviceAddress)

                //Sending user info only if it has changed
                val myUser = session.getUser()
                val updatedUser = if (message.userHash != myUser.hashCode()) myUser else null

                val privateChatExists = chatDataSource.getPrivateChatById(chatId = deviceAddress) != null
                val groupChatsInfo = message.receiverAdminGroupChatsInfo.map { chatInfo ->
                    //sending chat info only if it has changed
                    val currentChat = chatDataSource.getGroupChatById(chatId = chatInfo.chatId)
                    if (currentChat != null) {
                        val updatedChat = if (chatInfo.chatHash != currentChat.hashCode()) {
                            currentChat
                        } else {
                            null
                        }

                        val missingHistory = if (chatInfo.chatLastMessageId != null) {
                            messageDataSource.getAllNewerThan(
                                chatId = chatInfo.chatId,
                                messageId = chatInfo.chatLastMessageId,
                                limit = GroupChatHistorySyncLimit,
                            )
                        } else {
                            messageDataSource.getAll(chatId = chatInfo.chatId, limit = GroupChatHistorySyncLimit)
                        }

                        BtGroupChatInfo.Exists(
                            chatId = chatInfo.chatId,
                            chat = updatedChat?.toBluetooth(),
                            messages = missingHistory.map { it.toBluetooth() },
                        )
                    } else {
                        BtGroupChatInfo.Deleted(chatId = chatInfo.chatId)
                    }
                }

                val myGroupChatsHostedByUser = chatDataSource.getGroupChatByHostDeviceAddress(address = deviceAddress)
                val btGroupChatClientInfo = message.receiverClientGroupChatIds.map { chatId ->
                    BtGroupChatClientInfo(chatId = chatId, exists = myGroupChatsHostedByUser.any { it.id == chatId })
                }

                val response = messageManager.createInitConnectionResponseMessage(
                    receiverDeviceAddress = deviceAddress,
                    user = updatedUser,
                    hostTimestamp = System.currentTimeMillis(),
                    privateChatExists = privateChatExists,
                    groupChatsInfo = groupChatsInfo,
                    btGroupChatClientInfo = btGroupChatClientInfo,
                )
                sendMessage(message = response, deviceAddresses = listOf(deviceAddress))
            }

            is Protocol.InitConnection.Response -> {
                initMyUserDeviceAddress(deviceAddress = message.receiverDeviceAddress)

                message.user?.let {
                    userDataSource.save(it.toDomain())
                    fileDelegate.downloadUserPicIfMissing(user = message.user, hostDeviceAddress = deviceAddress)
                }

                message.groupChatsInfo.forEach { chatInfo ->
                    when (chatInfo) {
                        is BtGroupChatInfo.Deleted -> {
                            chatDataSource.setGroupChatDoesNotExist(chatId = chatInfo.chatId)
                        }

                        is BtGroupChatInfo.Exists -> {
                            groupChatManager.onChatUpdated(
                                hostTimestamp = message.hostTimestamp,
                                chatId = chatInfo.chatId,
                                chat = chatInfo.chat,
                                messages = chatInfo.messages,
                            )
                        }
                    }
                }

                message.btGroupChatClientInfo.filter { !it.exists }
                    .onEach {
                        groupChatManager.onUserLeft(
                            chatId = it.chatId,
                            userAddress = deviceAddress,
                            excludeFromUpdateMessage = true,
                        )
                    }

                if (!message.privateChatExists) {
                    chatDataSource.setPrivateChatDoesNotExist(userDeviceAddress = deviceAddress)
                }

                //Connection is established
                connectingDevicesFlow.value = connectingDevicesFlow.value.toMutableSet().apply { remove(deviceAddress) }
            }
        }
    }

    private suspend fun sendMessage(message: String, deviceAddresses: List<String>) {
        withContext(dispatcherManager.io) {
            connectionManager.sendMessage(message = message, receiverDevicesAddresses = deviceAddresses)
        }
    }

    private suspend fun initConnection(deviceAddress: String) {
        val user = userDataSource.get(deviceAddress)

        val groupChats = chatDataSource.getAllGroupChats()
        val userHostedGroupChats = groupChats.filter { it.hostDeviceAddress == deviceAddress }
        val userClientGroupChatIds = groupChats.filter { chat ->
            session.isCurrentUser(chat.hostDeviceAddress) && chat.users.any { it.deviceAddress == deviceAddress }
        }.map { it.id }
        val myUserHostedGroupChats = userHostedGroupChats.filter { chat ->
            chat.users.any { session.isCurrentUser(deviceAddress = it.deviceAddress) }
        }
        val myUserHostedGroupChatsHashInfo = myUserHostedGroupChats.map { chat ->
            BtGroupChatHashInfo(
                chatId = chat.id,
                chatHash = chat.hashCode(),
                chatLastMessageId = messageDataSource.getLast(chatId = chat.id)?.id,
            )
        }

        val response = messageManager.createInitConnectionRequestMessage(
            receiverDeviceAddress = deviceAddress,
            userHash = user?.hashCode(),
            receiverAdminGroupChatsHashInfo = myUserHostedGroupChatsHashInfo,
            receiverClientGroupChatIds = userClientGroupChatIds,
        )
        sendMessage(message = response, deviceAddresses = listOf(deviceAddress))
    }

    private suspend fun initMyUserDeviceAddress(deviceAddress: String) {
        session.setUserAddressIfEmpty(deviceAddress)

        chatDataSource.getGroupChatByHostDeviceAddress(address = USER_DEVICE_ADDRESS_NOT_SET).forEach { chat ->
            chatDataSource.deleteUserFromGroupChat(
                chatId = chat.id,
                userDeviceAddress = USER_DEVICE_ADDRESS_NOT_SET
            )

            chatDataSource.save(
                chat.copy(
                    hostDeviceAddress = deviceAddress,
                    users = listOf(session.getUser()), //TODO: somewhat dangerous??
                )
            )
        }
    }

}
