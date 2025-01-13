package com.bluetoothchat.core.ui.components.dialog

import androidx.compose.runtime.Composable
import com.bluetoothchat.core.ui.components.dialog.model.DialogInputParams
import com.bluetoothchat.core.ui.components.dialog.model.DialogOption
import com.bluetoothchat.core.ui.components.dialog.model.DialogResult
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.result.ResultBackNavigator
import com.ramcosta.composedestinations.spec.DestinationStyle

@Destination(style = DestinationStyle.Dialog::class)
@Composable
fun Dialog(
    params: DialogInputParams,
    resultNavigator: ResultBackNavigator<DialogResult>,
) {
    when (params) {
        is DialogInputParams.TextDialog -> {
            TextDialog(
                title = params.title,
                message = params.message,
                confirmButton = params.confirmButton,
                cancelButton = params.cancelButton,
                onDismissRequest = {
                    resultNavigator.navigateBack(
                        result = DialogResult(
                            dialogId = params.id,
                            actionPayload = params.actionPayload,
                            option = null,
                        ),
                        onlyIfResumed = true,
                    )
                },
                onOptionSelected = { button ->
                    resultNavigator.navigateBack(
                        result = DialogResult(
                            dialogId = params.id,
                            actionPayload = params.actionPayload,
                            option = DialogOption.ActionButton(id = button.id)
                        ),
                        onlyIfResumed = true,
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
                    resultNavigator.navigateBack(
                        result = DialogResult(
                            dialogId = params.id,
                            actionPayload = params.actionPayload,
                            option = null,
                        ),
                        onlyIfResumed = true,
                    )
                },
                onOptionSelected = { option ->
                    resultNavigator.navigateBack(
                        result = DialogResult(
                            dialogId = params.id,
                            actionPayload = params.actionPayload,
                            option = option
                        ),
                        onlyIfResumed = true,
                    )
                },
            )
        }
    }
}
