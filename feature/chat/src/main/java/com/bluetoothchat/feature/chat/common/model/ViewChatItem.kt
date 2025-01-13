package com.bluetoothchat.feature.chat.common.model

import com.bluetoothchat.core.ui.model.ViewMessage

sealed interface ViewChatItem {

    data class DateHeader(val date: String) : ViewChatItem

    data class Message(val message: ViewMessage, val extendedTopPadding: Boolean) : ViewChatItem

}

fun ViewChatItem.toId() = when(this){
    is ViewChatItem.DateHeader -> date
    is ViewChatItem.Message -> message.id
}
