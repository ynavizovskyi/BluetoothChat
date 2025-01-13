package com.bluetoothchat.core.ui.components.dialog.core

import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.bluetoothchat.core.ui.components.dialog.model.DialogButton
import com.bluetoothchat.core.ui.components.dialog.model.toColor

@Composable
internal fun DialogButton(button: DialogButton, clickListener: (DialogButton) -> Unit) {
    TextButton(
        onClick = { clickListener(button) },
        colors = ButtonDefaults.textButtonColors(contentColor = button.toColor())
    ) {
        Text(
            text = button.text.asString(),
            style = TextStyle(
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = LocalContentColor.current,
                letterSpacing = 0.1.sp,
                fontFamily = FontFamily.SansSerif
            ),
        )
    }
}
