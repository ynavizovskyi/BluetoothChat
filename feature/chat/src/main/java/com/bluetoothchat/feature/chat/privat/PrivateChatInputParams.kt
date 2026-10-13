package com.bluetoothchat.feature.chat.privat

import kotlinx.serialization.Serializable

@Serializable
data class PrivateChatInputParams(val chatId: String, val source: String)
