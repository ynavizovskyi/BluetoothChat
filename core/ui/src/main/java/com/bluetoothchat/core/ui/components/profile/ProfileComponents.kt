package com.bluetoothchat.core.ui.components.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.style.TextAlign
import com.bluetoothchat.core.ui.ToolbarActionSize
import com.bluetoothchat.core.ui.components.CustomTextFieldValueTextStyle
import com.bluetoothchat.core.ui.theme.LocalChatAppColorScheme

@Composable
fun ProfileFieldValueText(text: String, modifier: Modifier = Modifier) {
    Box(modifier = modifier) {
        Text(
            modifier = Modifier.align(Alignment.CenterStart),
            text = text,
            textAlign = TextAlign.Center,
            style = CustomTextFieldValueTextStyle.copy(
                color = LocalChatAppColorScheme.current.onScreenBackground,
            ),
        )
    }
}

@Composable
fun ProfileFieldNameText(text: String, modifier: Modifier = Modifier) {
    Text(
        modifier = modifier,
        text = text,
        style = ProfileFieldTitleTextStyle.copy(
            color = LocalChatAppColorScheme.current.onScreenBackground.copy(alpha = 0.5f),
        ),
    )
}

@Composable
fun ProfileImageActionIcon(painter: Painter, clickListener: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(color = LocalChatAppColorScheme.current.accent)
            .size(ToolbarActionSize)
            .clickable { clickListener() },
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painter,
            tint = LocalChatAppColorScheme.current.onToolbar,
            contentDescription = "Image action",
        )
    }
}
