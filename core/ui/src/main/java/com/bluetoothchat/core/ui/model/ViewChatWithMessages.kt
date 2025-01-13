package com.bluetoothchat.core.ui.model

data class ViewChatWithMessages(val chat: ViewChat, val lastMessage: ViewMessage?, val numOfUnreadMessages: Int)
