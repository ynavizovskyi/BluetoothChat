package com.bluetoothchat.core.ui.model.mapper.chat

import com.bluetoothchat.core.domain.model.Chat
import com.bluetoothchat.core.ui.model.ViewChatAction
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ViewPrivateChatActionMapper @Inject constructor() {

    suspend fun map(chat: Chat.Private, connectedDevices: List<String>): List<ViewChatAction> {
        val isConnected = connectedDevices.contains(chat.user.deviceAddress)
        return if (isConnected) {
            listOf(
                ViewChatAction.PrivateChat.Disconnect(chatId = chat.id),
                ViewChatAction.PrivateChat.DeleteAndDisconnect(chatId = chat.id),
            )
        } else {
            listOf(ViewChatAction.PrivateChat.Delete(chatId = chat.id))
        }
    }

}
