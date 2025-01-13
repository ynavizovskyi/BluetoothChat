package com.bluetoothchat.core.ui.model.mapper.chat

import com.bluetoothchat.core.domain.model.Chat
import com.bluetoothchat.core.ui.model.ViewChat
import com.bluetoothchat.core.ui.model.mapper.ViewUserMapper
import com.bluetoothchat.core.ui.util.TimeFormatType
import com.bluetoothchat.core.ui.util.TimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ViewPrivateChatMapper @Inject constructor(
    private val userMapper: ViewUserMapper,
    private val actionsMapper: ViewPrivateChatActionMapper,
    private val timeFormatter: TimeFormatter,
) {

    suspend fun map(chat: Chat.Private, connectedDevices: List<String>): ViewChat.Private {
        val user = userMapper.map(user = chat.user, connectedDevices = connectedDevices)
        return ViewChat.Private(
            createdTimestamp = chat.createdTimestamp,
            formattedTime = timeFormatter.formatTimeDate(
                timestamp = chat.createdTimestamp,
                type = TimeFormatType.TIME_IF_TODAY_DATE_OTHERWISE,
            ),
            exists = chat.exists,
            user = user,
            actions = actionsMapper.map(chat = chat, connectedDevices = connectedDevices),
        )
    }

}
