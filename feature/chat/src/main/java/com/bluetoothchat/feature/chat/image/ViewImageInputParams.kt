package com.bluetoothchat.feature.chat.image

import kotlinx.serialization.Serializable

@Serializable
data class ViewImageInputParams(val chatId: String, val messageId: String, val source: String)
