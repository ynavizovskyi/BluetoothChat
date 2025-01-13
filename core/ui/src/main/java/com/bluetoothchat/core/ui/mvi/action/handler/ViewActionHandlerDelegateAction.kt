package com.bluetoothchat.core.ui.mvi.action.handler

import com.bluetoothchat.core.ui.components.dialog.model.DialogResult
import com.bluetoothchat.core.ui.mvi.action.ViewAction

sealed interface ActionHandlerDelegateAction<T : ViewAction> {

    data class ActionClicked<T : ViewAction>(val action: T) : ActionHandlerDelegateAction<T>

    data class OnDialogResult<T : ViewAction>(val result: DialogResult) : ActionHandlerDelegateAction<T>

}
