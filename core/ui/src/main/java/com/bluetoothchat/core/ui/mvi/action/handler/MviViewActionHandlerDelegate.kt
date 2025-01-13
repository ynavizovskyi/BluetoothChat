package com.bluetoothchat.core.ui.mvi.action.handler

import com.bluetoothchat.core.ui.R
import com.bluetoothchat.core.ui.components.dialog.model.DialogButton
import com.bluetoothchat.core.ui.components.dialog.model.DialogButtonStyle
import com.bluetoothchat.core.ui.components.dialog.model.DialogInputParams
import com.bluetoothchat.core.ui.components.dialog.model.DialogOption
import com.bluetoothchat.core.ui.model.UiText
import com.bluetoothchat.core.ui.model.UiTextParam
import com.bluetoothchat.core.ui.model.UiTextParamStyle
import com.bluetoothchat.core.ui.mvi.action.ViewAction
import com.bluetoothchat.core.ui.mvi.action.ViewActionConfirmation
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow

abstract class MviViewActionHandlerDelegate<Action : ViewAction, OneTimeEvent : ActionHandlerDelegateEvent> {

    private val _oneTimeEvent: Channel<OneTimeEvent> = Channel(Channel.BUFFERED)
    val oneTimeEvent = _oneTimeEvent.receiveAsFlow()

    protected abstract val confirmationDialogId: Int

    protected suspend fun sendEvent(builder: () -> OneTimeEvent) {
        val event = builder()
        _oneTimeEvent.send(event)
    }

    fun shouldHandleDialogResult(id: Int) = id == confirmationDialogId

    abstract suspend fun onActionConfirmed(action: Action)
    abstract suspend fun showConfirmActionDialog(chatAction: Action, confirmation: ViewActionConfirmation.Dialog)

    suspend fun handleAction(action: ActionHandlerDelegateAction<Action>) {
        when (action) {
            is ActionHandlerDelegateAction.ActionClicked -> handleActionClicked(action)
            is ActionHandlerDelegateAction.OnDialogResult -> handleOnDialogResult(action)
        }
    }

    private suspend fun handleActionClicked(action: ActionHandlerDelegateAction.ActionClicked<Action>) {
        when (val confirmation = action.action.confirmation) {
            is ViewActionConfirmation.None -> onActionConfirmed(action = action.action)
            is ViewActionConfirmation.Dialog -> showConfirmActionDialog(
                chatAction = action.action,
                confirmation = confirmation,
            )
        }
    }

    private suspend fun handleOnDialogResult(action: ActionHandlerDelegateAction.OnDialogResult<Action>) {
        when {
            action.result.option is DialogOption.ActionButton && action.result.actionPayload != null -> {
                if (action.result.option.id == BUTTON_ID_CONFIRM_ACTION) {
                    onActionConfirmed(action = action.result.actionPayload as Action)
                }
            }

            else -> Unit //Cancelled
        }
    }

    protected fun createConfirmDialogInputParams(
        chatAction: Action,
        confirmation: ViewActionConfirmation.Dialog,
        messageParams: List<String>,
    ) = DialogInputParams.TextDialog(
        id = confirmationDialogId,
        actionPayload = chatAction,
        title = UiText.Resource(confirmation.titleStringRes),
        confirmButton = DialogButton(
            id = BUTTON_ID_CONFIRM_ACTION,
            text = UiText.Resource(confirmation.actionStringRes),
            style = DialogButtonStyle.RED,
        ),
        cancelButton = DialogButton(
            id = BUTTON_ID_CANCEL_ACTION,
            text = UiText.Resource(R.string.dialog_cancel),
        ),
        message = UiText.Resource(
            resId = confirmation.messageStringRes,
            params = messageParams.map { UiTextParam(text = it, style = UiTextParamStyle.BOLD) },
        ),
    )

    companion object {
        private const val BUTTON_ID_CONFIRM_ACTION = 1
        private const val BUTTON_ID_CANCEL_ACTION = 2
    }
}
