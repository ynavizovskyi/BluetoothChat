package com.bluetoothchat.core.ui.model.mapper

import com.bluetoothchat.core.db.datasource.MessageDataSource
import com.bluetoothchat.core.domain.model.Message
import com.bluetoothchat.core.filemanager.file.FileState
import com.bluetoothchat.core.ui.model.ViewMessage
import com.bluetoothchat.core.ui.model.ViewQuotedMessage
import com.bluetoothchat.core.ui.model.ViewUser
import com.bluetoothchat.core.ui.model.primaryContent
import com.bluetoothchat.core.ui.util.TimeFormatType
import com.bluetoothchat.core.ui.util.TimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ViewMessageMapper @Inject constructor(
    private val messageDataSource: MessageDataSource,
    private val messageContentMapper: ViewMessageContentMapper,
    private val timeFormatter: TimeFormatter,
    private val actionsMapper: ViewMessageActionsMapper,
) {

    suspend fun map(
        chatId: String,
        message: Message,
        filesBeingDownloaded: List<FileState.Downloading>,
        currentUser: ViewUser,
        allUsers: List<ViewUser>,
        canSendMessages: Boolean = false,
        timeFormatType: TimeFormatType = TimeFormatType.ONLY_TIME,
        displayUserInfo: Boolean = false,
    ): ViewMessage {
        val isMine = message.userDeviceAddress == currentUser.deviceAddress
        val user = allUsers.firstOrNull { it.deviceAddress == message.userDeviceAddress }

        return when (message) {
            is Message.GroupUpdate -> {
                ViewMessage.GroupUpdate(
                    id = message.id,
                    userDeviceAddress = message.userDeviceAddress,
                    user = user,
                    isMine = isMine,
                    timestamp = message.timestamp,
                    formattedTime = timeFormatter.formatTimeDate(timestamp = message.timestamp, type = timeFormatType),
                    isReadByMe = message.isReadByMe,
                    updateType = message.updateType,
                    targetUserDeviceAddress = message.targetDeviceAddress,
                    targetUser = allUsers.firstOrNull { it.deviceAddress == message.targetDeviceAddress },
                )
            }

            is Message.Plain -> {
                val content = message.content.map {
                    messageContentMapper.map(
                        content = it,
                        chatId = chatId,
                        filesBeingDownloaded = filesBeingDownloaded,
                    )
                }

                val quotedMessage = message.quotedMessageId?.let { id ->
                    val message = messageDataSource.get(messageId = id) as? Message.Plain
                    message?.let {
                        val quotedMessageUser = allUsers.firstOrNull { it.deviceAddress == message.userDeviceAddress }
                        val content = message.content.map {
                            messageContentMapper.map(
                                content = it,
                                chatId = chatId,
                                filesBeingDownloaded = filesBeingDownloaded,
                            )
                        }

                        ViewQuotedMessage(
                            id = message.id,
                            userDeviceAddress = message.userDeviceAddress,
                            primaryContent = content.primaryContent(),
                            user = quotedMessageUser,
                        )
                    }
                }

                ViewMessage.Plain(
                    id = message.id,
                    userDeviceAddress = message.userDeviceAddress,
                    user = user,
                    isMine = isMine,
                    timestamp = message.timestamp,
                    formattedTime = timeFormatter.formatTimeDate(timestamp = message.timestamp, type = timeFormatType),
                    isReadByMe = message.isReadByMe,
                    content = content,
                    displayUserImage = displayUserInfo,
                    displayUserName = displayUserInfo,
                    quotedMessage = quotedMessage,
                    actions = actionsMapper.map(content = content, canSendMessages = canSendMessages),
                )
            }
        }
    }

}
