package com.bluetoothchat.core.ui.model.mapper.chat

import com.bluetoothchat.core.domain.model.Chat
import com.bluetoothchat.core.ui.model.ViewChat
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ViewChatMapper @Inject constructor(
    private val privateChatMapper: ViewPrivateChatMapper,
    private val groupChatMapper: ViewGroupChatMapper,
) {

    suspend fun map(chat: Chat, connectedDevices: List<String>): ViewChat = when (chat) {
        is Chat.Private -> privateChatMapper.map(chat = chat, connectedDevices = connectedDevices)
        is Chat.Group -> groupChatMapper.map(chat = chat, connectedDevices = connectedDevices)
    }

}
