package com.bluetoothchat.core.ui.delegate

import com.bluetoothchat.core.bluetooth.message.manager.CommunicationManagerImpl
import com.bluetoothchat.core.db.datasource.ChatDataSource
import com.bluetoothchat.core.filemanager.file.FileManager
import com.bluetoothchat.core.ui.model.ViewChatAction
import com.bluetoothchat.core.ui.mvi.action.ViewActionConfirmation
import com.bluetoothchat.core.ui.mvi.action.handler.MviViewActionHandlerDelegate
import kotlinx.coroutines.delay
import javax.inject.Inject

//Should not be a singleton to avoid events slipping to other screens
class ChatActionHandlerDelegate @Inject constructor(
    private val communicationManager: CommunicationManagerImpl,
    private val chatDataSource: ChatDataSource,
    private val fileManager: FileManager,
) : MviViewActionHandlerDelegate<ViewChatAction, ChatActionHandlerDelegateEvent>() {

    override val confirmationDialogId: Int = 3001

    override suspend fun showConfirmActionDialog(
        chatAction: ViewChatAction,
        confirmation: ViewActionConfirmation.Dialog
    ) {
        val chat = chatDataSource.getChatById(chatId = chatAction.chatId) ?: return
        val dialogParams = createConfirmDialogInputParams(
            chatAction = chatAction,
            confirmation = confirmation,
            messageParams = listOf(chat.name),
        )
        sendEvent { ChatActionHandlerDelegateEvent.ShowDialog(params = dialogParams) }
    }

    override suspend fun onActionConfirmed(action: ViewChatAction) {
        when (action) {
            is ViewChatAction.PrivateChat.Disconnect -> {
                communicationManager.disconnect(deviceAddress = action.chatId)
            }

            is ViewChatAction.PrivateChat.Delete, is ViewChatAction.PrivateChat.DeleteAndDisconnect -> {
                chatDataSource.deletePrivateChat(userDeviceAddress = action.chatId)
                fileManager.deleteChatFolder(chatId = action.chatId)
                communicationManager.disconnect(deviceAddress = action.chatId)
                sendEvent { ChatActionHandlerDelegateEvent.CloseChatScreen }
            }

            is ViewChatAction.GroupChat.Host.DisconnectAll -> {
                disconnectAllGroupChatUsers(chatId = action.chatId)
            }

            is ViewChatAction.GroupChat.Host.Delete, is ViewChatAction.GroupChat.Host.DeleteAndDisconnectAll -> {
                disconnectAllGroupChatUsers(chatId = action.chatId)
                chatDataSource.deleteGroupChat(chatId = action.chatId)
                fileManager.deleteChatFolder(chatId = action.chatId)
                sendEvent { ChatActionHandlerDelegateEvent.CloseChatScreen }
            }

            is ViewChatAction.GroupChat.Client.Disconnect -> {
                disconnectGroupChatHost(chatId = action.chatId)
            }

            is ViewChatAction.GroupChat.Client.LeaveGroup, is ViewChatAction.GroupChat.Client.LeaveGroupAndDisconnect -> {

                communicationManager.leaveChat(chatId = action.chatId)
                disconnectGroupChatHost(chatId = action.chatId)

                //Making sure that the leave message has a decent chance of being sent before disconnecting
                delay(300)
                chatDataSource.deleteGroupChat(chatId = action.chatId)
                fileManager.deleteChatFolder(chatId = action.chatId)
                sendEvent { ChatActionHandlerDelegateEvent.CloseChatScreen }
            }
        }
    }

    private suspend fun disconnectAllGroupChatUsers(chatId: String) {
        val chat = chatDataSource.getGroupChatById(chatId = chatId)
        chat?.users?.forEach { user ->
            communicationManager.disconnect(deviceAddress = user.deviceAddress)
        }
    }

    private suspend fun disconnectGroupChatHost(chatId: String) {
        val chat = chatDataSource.getGroupChatById(chatId = chatId)
        chat?.let {
            communicationManager.disconnect(deviceAddress = chat.hostDeviceAddress)
        }
    }
}
