package com.bluetoothchat.feature.chat.group

import kotlinx.serialization.Serializable

@Serializable
data class GroupChatInputParams(val chatId: String, val source: String)
