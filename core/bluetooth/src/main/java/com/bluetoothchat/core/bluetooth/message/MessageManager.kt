package com.bluetoothchat.core.bluetooth.message

import com.bluetoothchat.core.bluetooth.message.model.BtParseMessageResult
import com.bluetoothchat.core.bluetooth.message.model.MsgComponentDivider
import com.bluetoothchat.core.bluetooth.message.model.Protocol
import com.bluetoothchat.core.bluetooth.message.model.ProtocolVersion
import com.bluetoothchat.core.bluetooth.message.model.entity.BtFileType
import com.bluetoothchat.core.bluetooth.message.model.entity.BtGroupChatClientInfo
import com.bluetoothchat.core.bluetooth.message.model.entity.BtGroupChatHashInfo
import com.bluetoothchat.core.bluetooth.message.model.entity.BtGroupChatInfo
import com.bluetoothchat.core.bluetooth.message.model.entity.toBluetooth
import com.bluetoothchat.core.domain.model.Chat
import com.bluetoothchat.core.domain.model.Message
import com.bluetoothchat.core.domain.model.User
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MessageManager @Inject constructor(
    private val json: Json,
) {

    suspend fun parseMessage(message: String): BtParseMessageResult {
        val components = message.split(MsgComponentDivider)
        val theirProtocolVersion = components[0].toInt()
        val type = components[1]
        val content = components[2]

        return if (theirProtocolVersion != ProtocolVersion) {
            BtParseMessageResult.Error.IncompatibleProtocols(
                myProtocolVersion = ProtocolVersion,
                theirProtocolVersion = theirProtocolVersion,
            )
        } else {
            val btMessage = when (type) {
                //Common
                Protocol.InitConnection.Request.type ->
                    json.decodeFromString<Protocol.InitConnection.Request>(content)

                Protocol.InitConnection.Response.type ->
                    json.decodeFromString<Protocol.InitConnection.Response>(content)

                Protocol.File.Request.type ->
                    json.decodeFromString<Protocol.File.Request>(content)

                Protocol.File.Response.type ->
                    json.decodeFromString<Protocol.File.Response>(content)
                //Group
                Protocol.GroupChat.InviteToChatRequest.type ->
                    json.decodeFromString<Protocol.GroupChat.InviteToChatRequest>(content)

                Protocol.GroupChat.InviteToChatResponse.type ->
                    json.decodeFromString<Protocol.GroupChat.InviteToChatResponse>(content)

                Protocol.GroupChat.ChatInitiationMessage.type ->
                    json.decodeFromString<Protocol.GroupChat.ChatInitiationMessage>(content)

                Protocol.GroupChat.HostChatMessage.type ->
                    json.decodeFromString<Protocol.GroupChat.HostChatMessage>(content)

                Protocol.GroupChat.ClientChatMessage.type ->
                    json.decodeFromString<Protocol.GroupChat.ClientChatMessage>(content)

                Protocol.GroupChat.ChatInfoRequest.type ->
                    json.decodeFromString<Protocol.GroupChat.ChatInfoRequest>(content)

                Protocol.GroupChat.ChatInfoResponse.type ->
                    json.decodeFromString<Protocol.GroupChat.ChatInfoResponse>(content)

                Protocol.GroupChat.UserInfoRequest.type ->
                    json.decodeFromString<Protocol.GroupChat.UserInfoRequest>(content)

                Protocol.GroupChat.UserInfoResponse.type ->
                    json.decodeFromString<Protocol.GroupChat.UserInfoResponse>(content)

                Protocol.GroupChat.FileReady.type ->
                    json.decodeFromString<Protocol.GroupChat.FileReady>(content)

                Protocol.GroupChat.LeaveChatReqeust.type ->
                    json.decodeFromString<Protocol.GroupChat.LeaveChatReqeust>(content)

                //Private
                Protocol.PrivateChat.InviteToChatRequest.type ->
                    json.decodeFromString<Protocol.PrivateChat.InviteToChatRequest>(content)

                Protocol.PrivateChat.InviteToChatResponse.type ->
                    json.decodeFromString<Protocol.PrivateChat.InviteToChatResponse>(content)

                Protocol.PrivateChat.ChatInitiationMessage.type ->
                    json.decodeFromString<Protocol.PrivateChat.ChatInitiationMessage>(content)

                Protocol.PrivateChat.ChatMessage.type ->
                    json.decodeFromString<Protocol.PrivateChat.ChatMessage>(content)

                Protocol.PrivateChat.UserInfoRequest.type ->
                    json.decodeFromString<Protocol.PrivateChat.UserInfoRequest>(content)

                Protocol.PrivateChat.UserInfoResponse.type ->
                    json.decodeFromString<Protocol.PrivateChat.UserInfoResponse>(content)

                else -> error("Unknown message type $type")
            }
            BtParseMessageResult.Success(message = btMessage)
        }
    }

    //Common
    fun createInitConnectionRequestMessage(
        receiverDeviceAddress: String,
        userHash: Int?,
        receiverAdminGroupChatsHashInfo: List<BtGroupChatHashInfo>,
        receiverClientGroupChatIds: List<String>
    ): String {
        val model = Protocol.InitConnection.Request(
            receiverDeviceAddress = receiverDeviceAddress,
            userHash = userHash,
            receiverAdminGroupChatsInfo = receiverAdminGroupChatsHashInfo,
            receiverClientGroupChatIds = receiverClientGroupChatIds,

            )
        return createBtMessage(
            modelString = json.encodeToString(model),
            type = Protocol.InitConnection.Request.type,
        )
    }

    fun createInitConnectionResponseMessage(
        receiverDeviceAddress: String,
        user: User?,
        hostTimestamp: Long,
        privateChatExists: Boolean,
        groupChatsInfo: List<BtGroupChatInfo>,
        btGroupChatClientInfo: List<BtGroupChatClientInfo>,
    ): String {
        val model = Protocol.InitConnection.Response(
            receiverDeviceAddress = receiverDeviceAddress,
            user = user?.toBluetooth(),
            hostTimestamp = hostTimestamp,
            privateChatExists = privateChatExists,
            groupChatsInfo = groupChatsInfo,
            btGroupChatClientInfo = btGroupChatClientInfo,
        )
        return createBtMessage(
            modelString = json.encodeToString(model),
            type = Protocol.InitConnection.Response.type,
        )
    }

    fun createFileRequest(fileType: BtFileType, chatId: String?, fileName: String): String {
        val model = Protocol.File.Request(
            fileType = fileType,
            chatId = chatId,
            fileName = fileName,
        )
        return createBtMessage(
            modelString = json.encodeToString(model),
            type = Protocol.File.Request.type,
        )
    }

    fun createFileResponse(fileType: BtFileType, chatId: String?, fileName: String, fileSize: Long): String {
        val model = Protocol.File.Response(
            fileType = fileType,
            chatId = chatId,
            fileName = fileName,
            fileSize = fileSize,
        )
        return createBtMessage(
            modelString = json.encodeToString(model),
            type = Protocol.File.Response.type,
        )
    }

    //Group
    fun createGroupChatInviteToChatRequest(chatId: String): String {
        val model = Protocol.GroupChat.InviteToChatRequest(chatId = chatId)
        return createBtMessage(
            modelString = json.encodeToString(model),
            type = Protocol.GroupChat.InviteToChatRequest.type,
        )
    }

    fun createGroupChatInviteToChatResponse(
        chatId: String,
        user: User,
        lastMessageId: String?,
    ): String {
        val model = Protocol.GroupChat.InviteToChatResponse(
            chatId = chatId,
            user = user.toBluetooth(),
            lastMessageId = lastMessageId,
        )
        return createBtMessage(
            modelString = json.encodeToString(model),
            type = Protocol.GroupChat.InviteToChatResponse.type,
        )
    }

    fun createGroupChatInitiationMessage(chat: Chat.Group, messages: List<Message>): String {
        val model = Protocol.GroupChat.ChatInitiationMessage(
            hostTimestamp = System.currentTimeMillis(),
            chat = chat.toBluetooth(),
            messages = messages.map { it.toBluetooth() },
        )
        return createBtMessage(
            modelString = json.encodeToString(model),
            type = Protocol.GroupChat.ChatInitiationMessage.type,
        )
    }

    fun createGroupChatHostMessage(chatId: String, chatHash: Int, message: Message): String {
        val model = Protocol.GroupChat.HostChatMessage(
            hostTimestamp = System.currentTimeMillis(),
            chatId = chatId,
            chatHash = chatHash,
            message = message.toBluetooth(),
        )
        return createBtMessage(
            modelString = json.encodeToString(model),
            type = Protocol.GroupChat.HostChatMessage.type,
        )
    }

    fun createGroupChatClientMessage(chatId: String, userHash: Int, message: Message): String {
        val model = Protocol.GroupChat.ClientChatMessage(
            chatId = chatId,
            userHash = userHash,
            message = message.toBluetooth(),
        )
        return createBtMessage(
            modelString = json.encodeToString(model),
            type = Protocol.GroupChat.ClientChatMessage.type,
        )
    }

    fun createGroupChatInfoRequest(chatId: String): String {
        val model = Protocol.GroupChat.ChatInfoRequest(chatId = chatId)
        return createBtMessage(
            modelString = json.encodeToString(model),
            type = Protocol.GroupChat.ChatInfoRequest.type,
        )
    }

    fun createGroupChatInfoResponse(chat: Chat.Group): String {
        val model = Protocol.GroupChat.ChatInfoResponse(chat = chat.toBluetooth())
        return createBtMessage(
            modelString = json.encodeToString(model),
            type = Protocol.GroupChat.ChatInfoResponse.type,
        )
    }

    fun createGroupChatUserInfoRequest(address: String): String {
        val model = Protocol.GroupChat.UserInfoRequest(address = address)
        return createBtMessage(
            modelString = json.encodeToString(model),
            type = Protocol.GroupChat.UserInfoRequest.type,
        )
    }

    fun createGroupChatUserInfoResponse(user: User): String {
        val model = Protocol.GroupChat.UserInfoResponse(user = user.toBluetooth())
        return createBtMessage(
            modelString = json.encodeToString(model),
            type = Protocol.GroupChat.UserInfoResponse.type,
        )
    }

    fun createGroupChatFileReadyMessage(fileType: BtFileType, chatId: String, fileName: String): String {
        val model = Protocol.GroupChat.FileReady(
            fileType = fileType,
            chatId = chatId,
            fileName = fileName,
        )
        return createBtMessage(
            modelString = json.encodeToString(model),
            type = Protocol.GroupChat.FileReady.type,
        )
    }

    fun createLeaveChatRequestMessage(chatId: String): String {
        val model = Protocol.GroupChat.LeaveChatReqeust(chatId = chatId)
        return createBtMessage(
            modelString = json.encodeToString(model),
            type = Protocol.GroupChat.LeaveChatReqeust.type,
        )
    }

    //Private
    fun createPrivateChatInviteToChatRequest(): String {
        val model = Protocol.PrivateChat.InviteToChatRequest(dummy = "")
        return createBtMessage(
            modelString = json.encodeToString(model),
            type = Protocol.PrivateChat.InviteToChatRequest.type,
        )
    }

    fun createPrivateChatInviteToChatResponse(user: User): String {
        val model = Protocol.PrivateChat.InviteToChatResponse(user = user.toBluetooth())
        return createBtMessage(
            modelString = json.encodeToString(model),
            type = Protocol.PrivateChat.InviteToChatResponse.type,
        )
    }

    fun createPrivateChatInitiationMessage(user: User): String {
        val model = Protocol.PrivateChat.ChatInitiationMessage(user = user.toBluetooth())
        return createBtMessage(
            modelString = json.encodeToString(model),
            type = Protocol.PrivateChat.ChatInitiationMessage.type,
        )
    }

    fun createPrivateChaMessage(message: Message, userHash: Int): String {
        val model = Protocol.PrivateChat.ChatMessage(message = message.toBluetooth(), userHash = userHash)
        return createBtMessage(
            modelString = json.encodeToString(model),
            type = Protocol.PrivateChat.ChatMessage.type,
        )
    }

    fun createPrivateChatUserInfoRequest(userDeviceAddress: String): String {
        val model = Protocol.PrivateChat.UserInfoRequest(userDeviceAddress = userDeviceAddress)
        return createBtMessage(
            modelString = json.encodeToString(model),
            type = Protocol.PrivateChat.UserInfoRequest.type,
        )
    }

    fun createPrivateChatUserInfoResponse(user: User): String {
        val model = Protocol.PrivateChat.UserInfoResponse(user = user.toBluetooth())
        return createBtMessage(
            modelString = json.encodeToString(model),
            type = Protocol.PrivateChat.UserInfoResponse.type,
        )
    }

    private fun createBtMessage(modelString: String, type: String): String {
        return "$ProtocolVersion$MsgComponentDivider$type$MsgComponentDivider${modelString}"
    }

}
