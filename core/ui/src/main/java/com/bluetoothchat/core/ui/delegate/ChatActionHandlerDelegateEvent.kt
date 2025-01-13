package com.bluetoothchat.core.ui.delegate

import com.bluetoothchat.core.ui.components.dialog.model.DialogInputParams
import com.bluetoothchat.core.ui.mvi.action.handler.ActionHandlerDelegateEvent

sealed interface ChatActionHandlerDelegateEvent : ActionHandlerDelegateEvent {

    object CloseChatScreen : ChatActionHandlerDelegateEvent

    data class ShowDialog(val params: DialogInputParams) : ChatActionHandlerDelegateEvent

}
