package com.bluetoothchat.core.ui.model

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.bluetoothchat.core.filemanager.file.FileState
import com.bluetoothchat.core.ui.R
import com.bluetoothchat.core.ui.theme.LocalChatAppColorScheme
import com.bluetoothchat.core.ui.theme.mainItemMessageText

sealed interface ViewMessageContent {

    data class Text(val text: String) : ViewMessageContent

    data class Image(val file: FileState, val aspectRatio: Float, val dominantColor: Int) : ViewMessageContent

}

fun List<ViewMessageContent>.primaryContent() = first()

@Composable
fun ViewMessageContent.toShortDescription() = when (this) {
    is ViewMessageContent.Text -> text
    is ViewMessageContent.Image -> stringResource(id = R.string.chat_image)
}

@Composable
fun ViewMessageContent.toShortColor() = when (this) {
    is ViewMessageContent.Text -> LocalChatAppColorScheme.current.mainItemMessageText
    is ViewMessageContent.Image -> LocalChatAppColorScheme.current.accent
}
