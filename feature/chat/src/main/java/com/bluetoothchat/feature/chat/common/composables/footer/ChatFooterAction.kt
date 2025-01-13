package com.bluetoothchat.feature.chat.common.composables.footer

internal sealed interface ChatFooterAction {

    data class SendMessageClicked(val text: String) : ChatFooterAction
    data class ButtonClicked(val id: Int) : ChatFooterAction
    object FileClicked : ChatFooterAction
    object ClearReplyClicked : ChatFooterAction

}
