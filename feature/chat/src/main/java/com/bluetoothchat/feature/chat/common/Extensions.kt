package com.bluetoothchat.feature.chat.common

import androidx.compose.runtime.Composable
import com.bluetoothchat.core.ui.model.ViewMessage
import com.bluetoothchat.core.ui.theme.LocalChatAppColorScheme

@Composable
internal fun ViewMessage.toBackgroundColor() = if (this.isMine) {
    LocalChatAppColorScheme.current.mineMessageBackground
} else {
    LocalChatAppColorScheme.current.othersMessageBackground
}


@Composable
internal fun ViewMessage.toContentColor() = if (this.isMine) {
    LocalChatAppColorScheme.current.mineMessageContent
} else {
    LocalChatAppColorScheme.current.othersMessageContent
}
