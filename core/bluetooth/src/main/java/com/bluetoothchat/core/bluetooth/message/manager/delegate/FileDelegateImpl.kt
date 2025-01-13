package com.bluetoothchat.core.bluetooth.message.manager.delegate

import com.bluetoothchat.core.bluetooth.connection.BtConnection
import com.bluetoothchat.core.bluetooth.connection.BtConnectionManager
import com.bluetoothchat.core.bluetooth.message.MessageManager
import com.bluetoothchat.core.bluetooth.message.manager.FileDelegate
import com.bluetoothchat.core.bluetooth.message.model.Protocol
import com.bluetoothchat.core.bluetooth.message.model.entity.BtFileType
import com.bluetoothchat.core.bluetooth.message.model.entity.BtGroupChat
import com.bluetoothchat.core.bluetooth.message.model.entity.BtUser
import com.bluetoothchat.core.dispatcher.DispatcherManager
import com.bluetoothchat.core.domain.model.Message
import com.bluetoothchat.core.domain.model.MessageContent
import com.bluetoothchat.core.domain.model.primary
import com.bluetoothchat.core.filemanager.file.FileManager
import com.bluetoothchat.core.filemanager.file.FileState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FileDelegateImpl @Inject constructor(
    private val dispatcherManager: DispatcherManager,
    private val connectionManager: BtConnectionManager,
    private val fileManager: FileManager,
    private val messageManager: MessageManager,
) : FileDelegate {

    override fun observeChatFilesBeingDownloaded(chatId: String): Flow<List<FileState.Downloading>> {
        return connectionManager.connectionState.flatMapLatest { connections ->
            combine(connections.map { it.state }) { connectionStates ->
                connectionStates.toList().filterIsInstance<BtConnection.State.FileTransfer.Receiving>()
            }
                .map { downloadingStates ->
                    downloadingStates.filter { it.chatId == chatId }
                        .map {
                            FileState.Downloading(
                                fileName = it.fileName,
                                fileSizeBytes = it.sizeBytes,
                                bytesDownloaded = it.bytesTransferred,
                            )
                        }
                }
        }
            .onStart { emit(emptyList()) }
            .flowOn(dispatcherManager.io)

    }

    override fun observeUserFilesBeingDownloaded(): Flow<List<FileState.Downloading>> {
        return connectionManager.connectionState.flatMapLatest { connections ->
            combine(connections.map { it.state }) { connectionStates ->
                connectionStates.toList().filterIsInstance<BtConnection.State.FileTransfer.Receiving>()
            }
                .map { downloadingStates ->
                    downloadingStates.filter { it.fileType == BtFileType.USER_FILE }
                        .map {
                            FileState.Downloading(
                                fileName = it.fileName,
                                fileSizeBytes = it.sizeBytes,
                                bytesDownloaded = it.bytesTransferred,
                            )
                        }
                }
        }
            .onStart { emit(emptyList()) }
            .flowOn(dispatcherManager.io)

    }

    override fun observeChatFileState(chatId: String, fileName: String, fileSizeBytes: Long): Flow<FileState> {
        return connectionManager.fileDownloadedEventFlow
//            .filter { it.fileType == BtFileType.CHAT_FILE && it.chatId == chatId && it.fileName == fileName }
            .map { fileManager.getChatFile(chatId = chatId, fileName = fileName, fileSizeBytes = fileSizeBytes) }
//            .onStart { emit(fileManager.getChatFile(chatId = chatId, fileName = fileName)) }
            .flowOn(dispatcherManager.io)
    }


    suspend fun downloadUserPicIfMissing(user: BtUser, hostDeviceAddress: String) = withContext(dispatcherManager.io) {
        user.picture?.let {
            if (fileManager.getChatAvatarPictureFile(fileName = it.id, sizeBytes = it.sizeBytes) is FileState.Missing) {
                val response = messageManager.createFileRequest(
                    fileType = BtFileType.USER_FILE,
                    chatId = null,
                    fileName = it.id,
                )
                sendMessage(message = response, deviceAddresses = listOf(hostDeviceAddress))
            }
        }
    }

    suspend fun downloadChatPicIfMissing(chat: BtGroupChat) = withContext(dispatcherManager.io) {
        chat.picture?.let {
            if (fileManager.getChatAvatarPictureFile(fileName = it.id, sizeBytes = it.sizeBytes) is FileState.Missing) {
                val response = messageManager.createFileRequest(
                    fileType = BtFileType.USER_FILE,
                    chatId = chat.id,
                    fileName = it.id,
                )
                sendMessage(message = response, deviceAddresses = listOf(chat.hostDeviceAddress))
            }
        }
    }

    suspend fun downloadContentFile(message: Message, deviceAddress: String, chatId: String?) =
        withContext(dispatcherManager.io) {
            (message as? Message.Plain)?.content?.primary()?.let { content ->
                if (content is MessageContent.File) {
                    downloadContentFile(content = content, deviceAddress = deviceAddress, chatId = chatId)
                }
            }
        }

    //chatId is null for private chats because the chat ids don't match
    suspend fun downloadContentFile(content: MessageContent.File, deviceAddress: String, chatId: String?) =
        withContext(dispatcherManager.io) {
            val response = messageManager.createFileRequest(
                fileType = BtFileType.CHAT_FILE,
                chatId = chatId,
                fileName = content.fileName
            )
            sendMessage(message = response, deviceAddresses = listOf(deviceAddress))
        }

    suspend fun handleMessage(message: Protocol.File, deviceAddress: String) {
        when (message) {
            is Protocol.File.Request -> {
                //If chatId is null this is private chat file with different chat ids on two devices
                val myChatId = message.chatId ?: deviceAddress
                val theirChatId = message.chatId
                val folderFile = when (message.fileType) {
                    BtFileType.USER_FILE -> fileManager.getOrCreateUsersFolder()
                    BtFileType.CHAT_FILE -> fileManager.getOrCreateChatFolder(chatId = myChatId)
                }
                val file = File(folderFile, message.fileName)
                if (file.exists()) {
                    val response = messageManager.createFileResponse(
                        fileType = message.fileType,
                        chatId = theirChatId,
                        fileName = message.fileName,
                        fileSize = file.length(),
                    )
                    sendMessage(message = response, deviceAddresses = listOf(deviceAddress))
                    sendFile(file = file, deviceAddress = deviceAddress)
                }
            }

            is Protocol.File.Response -> {} //Handled by BtConnection
        }
    }

    private suspend fun sendMessage(message: String, deviceAddresses: List<String>) {
        withContext(dispatcherManager.io) {
            connectionManager.sendMessage(message = message, receiverDevicesAddresses = deviceAddresses)
        }
    }

    private suspend fun sendFile(file: File, deviceAddress: String) {
        withContext(dispatcherManager.io) {
            connectionManager.sendFile(file = file, receiverDeviceAddress = deviceAddress)
        }
    }

}
