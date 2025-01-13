package com.bluetoothchat.feature.chat.privat.contract

import com.bluetoothchat.core.ui.model.ViewChat
import com.bluetoothchat.core.ui.model.ViewMessage
import com.bluetoothchat.core.ui.mvi.contract.ViewState
import com.bluetoothchat.feature.chat.common.composables.footer.ChatFooterState
import com.bluetoothchat.feature.chat.common.model.ViewChatItem

internal sealed interface PrivateChatState : ViewState {

    object Loading : PrivateChatState

    data class Loaded(
        val chat: ViewChat.Private,
        val items: List<ViewChatItem>,
        val quotedMessage: ViewMessage?,
        val footer: ChatFooterState,
        val isConnectActionPending: Boolean,
    ) : PrivateChatState

}
