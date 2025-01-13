package com.bluetoothchat.core.ui.model.mapper.chat

import com.bluetoothchat.core.domain.model.Chat
import com.bluetoothchat.core.filemanager.file.FileManager
import com.bluetoothchat.core.session.Session
import com.bluetoothchat.core.ui.model.ViewChat
import com.bluetoothchat.core.ui.model.mapper.ViewUserActionsMapper
import com.bluetoothchat.core.ui.model.mapper.ViewUserMapper
import com.bluetoothchat.core.ui.util.TimeFormatType
import com.bluetoothchat.core.ui.util.TimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ViewGroupChatMapper @Inject constructor(
    private val session: Session,
    private val fileManager: FileManager,
    private val userMapper: ViewUserMapper,
    private val actionsMapper: ViewGroupChatActionMapper,
    private val timeFormatter: TimeFormatter,
) {

    suspend fun map(
        chat: Chat.Group,
        connectedDevices: List<String>,
        userActionsMapper: ViewUserActionsMapper? = null,
    ): ViewChat.Group {
        val pictureFileState = chat.picture?.let {
            fileManager.getChatAvatarPictureFile(fileName = it.id, sizeBytes = it.sizeBytes)
        }
        val viewUsers = chat.users.map { user ->
            userMapper.map(user = user, connectedDevices = connectedDevices, userActionsMapper = userActionsMapper)
        }

        val connectedMembersNumber = if (session.isCurrentUser(chat.hostDeviceAddress)) {
            val count = viewUsers.filter { it.isConnected }.size
            //Add the host as connected device, don't show if only host connected
            if (count > 0) count + 1 else null
        } else {
            null
        }

        return ViewChat.Group(
            id = chat.id,
            createdTimestamp = chat.createdTimestamp,
            formattedTime = timeFormatter.formatTimeDate(
                timestamp = chat.createdTimestamp,
                type = TimeFormatType.TIME_IF_TODAY_DATE_OTHERWISE,
            ),
            exists = chat.exists,
            hostDeviceAddress = chat.hostDeviceAddress,
            connectedMembersNumber = connectedMembersNumber,
            name = chat.name,
            pictureFileState = pictureFileState,
            users = viewUsers,
            actions = actionsMapper.map(chat = chat, connectedDevices = connectedDevices),
        )
    }

}
