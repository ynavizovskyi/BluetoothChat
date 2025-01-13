package com.bluetoothchat.core.ui.model.mapper.chat

import com.bluetoothchat.core.domain.model.Chat
import com.bluetoothchat.core.session.Session
import com.bluetoothchat.core.ui.model.ViewChatAction
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ViewGroupChatActionMapper @Inject constructor(
    private val session: Session,
) {

    suspend fun map(chat: Chat.Group, connectedDevices: List<String>): List<ViewChatAction.GroupChat> {
        val isUserHost = session.isCurrentUser(chat.hostDeviceAddress)
        return if (isUserHost) {
            val anyClientConnected = chat.users.any { connectedDevices.contains(it.deviceAddress) }
            if (anyClientConnected) {
                listOf(
                    ViewChatAction.GroupChat.Host.DisconnectAll(chatId = chat.id),
                    ViewChatAction.GroupChat.Host.DeleteAndDisconnectAll(chatId = chat.id),
                )
            } else {
                listOf(ViewChatAction.GroupChat.Host.Delete(chatId = chat.id))
            }
        } else {
            val connectedToHost = connectedDevices.contains(chat.hostDeviceAddress)
            if (connectedToHost) {
                listOf(
                    ViewChatAction.GroupChat.Client.Disconnect(chatId = chat.id),
                    ViewChatAction.GroupChat.Client.LeaveGroupAndDisconnect(chatId = chat.id),
                )
            } else {
                listOf(ViewChatAction.GroupChat.Client.LeaveGroup(chatId = chat.id))
            }
        }
    }

}
