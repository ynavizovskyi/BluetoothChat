package com.bluetoothchat.core.ui.components.dialog

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.bluetoothchat.core.ui.components.dialog.core.Dialog
import com.bluetoothchat.core.ui.components.dialog.core.DialogButton
import com.bluetoothchat.core.ui.components.dialog.core.TitleTextStyle
import com.bluetoothchat.core.ui.components.dialog.model.DialogButton
import com.bluetoothchat.core.ui.components.dialog.model.DialogOption
import com.bluetoothchat.core.ui.components.dialog.model.DialogRadioButton
import com.bluetoothchat.core.ui.model.UiText

@Composable
internal fun RadioGroupDialog(
    title: UiText,
    radioButtons: List<DialogRadioButton>,
    confirmButton: DialogButton,
    onDismissRequest: () -> Unit,
    onOptionSelected: (DialogOption) -> Unit,
) {
    Dialog(
        title = {
            Text(
                text = title.asString(),
                style = TitleTextStyle,
            )
        },
        confirmButton = {
            DialogButton(
                button = confirmButton,
                clickListener = { onOptionSelected(DialogOption.ActionButton(id = it.id)) },
            )
        },
        content = {
            Column {
                radioButtons.forEach { button ->
                    DialogRadioButton(
                        button = button,
                        selectListener = { onOptionSelected(DialogOption.RadioButton(data = it.data)) },
                    )
                }
            }
        },
        onDismissRequest = onDismissRequest,
    )
}

@Composable
private fun DialogRadioButton(button: DialogRadioButton, selectListener: (DialogRadioButton) -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .selectable(
                selected = button.isSelected,
                onClick = { selectListener.invoke(button) }
            )
    ) {
        RadioButton(
            modifier = Modifier
                .align(alignment = Alignment.CenterVertically),
            selected = button.isSelected,
            onClick = { selectListener.invoke(button) }
        )
        Text(
            text = button.text.asString(),
            modifier = Modifier
                .align(alignment = Alignment.CenterVertically),
            style = TextStyle(
                fontSize = 16.sp,
                fontWeight = FontWeight.Normal,
                fontFamily = FontFamily.SansSerif
            ),
        )
    }
}
