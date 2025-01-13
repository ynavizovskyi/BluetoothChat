package com.bluetoothchat.core.bluetooth.message.manager.delegate

import com.bluetoothchat.core.bluetooth.message.manager.NotificationDelegate
import com.bluetoothchat.core.bluetooth.notification.NotificationManagerWrapper
import com.bluetoothchat.core.domain.model.Chat
import com.bluetoothchat.core.domain.model.Message
import com.bluetoothchat.core.domain.model.User
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationDelegateImpl @Inject constructor(
    private val notificationManager: NotificationManagerWrapper,
) : NotificationDelegate {

    var currentOpenChatId: String? = null
    var connectScreenOpen: Boolean = false

    override fun onChatScreenOpened(chatId: String) {
        currentOpenChatId = chatId
        notificationManager.hideChatNotifications(chatId = chatId)
    }

    override fun onChatScreenClosed(chatId: String) {
        currentOpenChatId = null
    }

    override fun onConnectScreenOpen() {
        connectScreenOpen = true
    }

    override fun onConnectScreenClosed() {
        connectScreenOpen = false
    }

    override suspend fun showChatMessageNotification(chat: Chat, message: Message.Plain, user: User?) {
        if (currentOpenChatId != chat.id) {
            notificationManager.showChatMessageNotification(message = message, chat = chat, user = user)
        }
    }

    override suspend fun showPrivateChatStartedNotification(chat: Chat.Private) {
        if (!connectScreenOpen) {
            notificationManager.showPrivateChatStartedNotification(chat = chat)
        }
    }

    override suspend fun showAddedToGroupChatNotification(chat: Chat.Group) {
        if (!connectScreenOpen) {
            notificationManager.showAddedToGroupChatNotification(chat = chat)
        }
    }
}
