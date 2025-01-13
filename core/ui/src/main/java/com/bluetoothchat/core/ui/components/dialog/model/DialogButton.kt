package com.bluetoothchat.core.ui.components.dialog.model

import android.os.Parcelable
import androidx.compose.runtime.Composable
import com.bluetoothchat.core.ui.R
import com.bluetoothchat.core.ui.components.dialog.core.dialogActionAccent
import com.bluetoothchat.core.ui.components.dialog.core.dialogActionNeutral
import com.bluetoothchat.core.ui.components.dialog.core.dialogActionRed
import com.bluetoothchat.core.ui.model.UiText
import com.bluetoothchat.core.ui.theme.LocalChatAppColorScheme
import kotlinx.parcelize.Parcelize

@Parcelize
data class DialogButton(
    val id: Int,
    val text: UiText,
    val style: DialogButtonStyle = DialogButtonStyle.ACCENT,
) : Parcelable

enum class DialogButtonStyle {
    NEUTRAL, ACCENT, RED
}

@Composable
fun DialogButton.toColor() = when(style){
    DialogButtonStyle.NEUTRAL -> LocalChatAppColorScheme.current.dialogActionNeutral
    DialogButtonStyle.ACCENT -> LocalChatAppColorScheme.current.dialogActionAccent
    DialogButtonStyle.RED -> LocalChatAppColorScheme.current.dialogActionRed
}

fun defaultDialogButton() = DialogButton(id = 0, text = UiText.Resource(R.string.dialog_ok))
