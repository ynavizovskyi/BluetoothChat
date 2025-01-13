package com.bluetoothchat.core.ui.components.dialog

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.bluetoothchat.core.ui.components.dialog.core.Dialog
import com.bluetoothchat.core.ui.components.dialog.core.DialogButton
import com.bluetoothchat.core.ui.components.dialog.core.MessageTextStyle
import com.bluetoothchat.core.ui.components.dialog.core.TitleTextStyle
import com.bluetoothchat.core.ui.components.dialog.model.DialogButton
import com.bluetoothchat.core.ui.model.UiText

@Composable
internal fun TextDialog(
    title: UiText,
    message: UiText,
    confirmButton: DialogButton,
    cancelButton: DialogButton?,
    onDismissRequest: () -> Unit,
    onOptionSelected: (DialogButton) -> Unit,
) {
    Dialog(
        title = {
            Text(
                text = title.asString(),
                style = TitleTextStyle,
            )
        },
        confirmButton = {
            DialogButton(button = confirmButton, clickListener = onOptionSelected)
        },
        dismissButton = cancelButton?.let {
            { DialogButton(button = cancelButton, clickListener = onOptionSelected) }
        },
        content = {
            Text(
                text = message.asAnnotatedString(),
                style = MessageTextStyle,
            )
        },
        onDismissRequest = onDismissRequest,
    )
}
