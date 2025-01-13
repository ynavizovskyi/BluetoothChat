package com.bluetoothchat.feature.chat.group.contract

import com.bluetoothchat.core.ui.model.ViewChat
import com.bluetoothchat.core.ui.model.ViewMessage
import com.bluetoothchat.core.ui.mvi.contract.ViewState
import com.bluetoothchat.feature.chat.common.composables.footer.ChatFooterState
import com.bluetoothchat.feature.chat.common.model.ViewChatItem

internal sealed interface GroupChatState : ViewState {

    object Loading : GroupChatState

    data class Loaded(
        val chat: ViewChat.Group,
        val items: List<ViewChatItem>,
        val quotedMessage: ViewMessage?,
        val footer: ChatFooterState,
        val isConnectActionPending: Boolean,
    ) : GroupChatState

}
