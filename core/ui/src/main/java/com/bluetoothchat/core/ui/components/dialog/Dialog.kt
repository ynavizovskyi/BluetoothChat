package com.bluetoothchat.core.ui.components.dialog

import androidx.compose.runtime.Composable
import com.bluetoothchat.core.ui.components.dialog.model.DialogInputParams
import com.bluetoothchat.core.ui.components.dialog.model.DialogOption
import com.bluetoothchat.core.ui.components.dialog.model.DialogResult

@Composable
fun Dialog(
    params: DialogInputParams,
    resultCallback: (DialogResult) -> Unit,
) {
    when (params) {
        is DialogInputParams.TextDialog -> {
            TextDialog(
                title = params.title,
                message = params.message,
                confirmButton = params.confirmButton,
                cancelButton = params.cancelButton,
                onDismissRequest = {
                    resultCallback(
                        DialogResult(
                            dialogId = params.id,
                            actionPayload = params.actionPayload,
                            option = null,
                        )
                    )
                },
                onOptionSelected = { button ->
                    resultCallback(
                        DialogResult(
                            dialogId = params.id,
                            actionPayload = params.actionPayload,
                            option = DialogOption.ActionButton(id = button.id)
                        )
                    )
                },
            )
        }

        is DialogInputParams.RadioGroupDialog -> {
            RadioGroupDialog(
                title = params.title,
                radioButtons = params.radioButtons,
                confirmButton = params.confirmButton,
                onDismissRequest = {
                    resultCallback(
                        DialogResult(
                            dialogId = params.id,
                            actionPayload = params.actionPayload,
                            option = null,
                        )
                    )
                },
                onOptionSelected = { option ->
                    resultCallback(
                        DialogResult(
                            dialogId = params.id,
                            actionPayload = params.actionPayload,
                            option = option
                        )
                    )
                },
            )
        }
    }
}
