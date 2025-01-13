package com.bluetoothchat.core.ui.components

import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.bluetoothchat.core.ui.theme.LocalChatAppColorScheme

@Composable
fun ChatAppActionSnackbar(snackbarData: SnackbarData) {
    Snackbar(
        action = {
            TextButton(onClick = { snackbarData.performAction() }) {
                Text(
                    text = snackbarData.visuals.actionLabel.orEmpty(),
                    color = LocalChatAppColorScheme.current.accent,
                    fontSize = 16.sp,
                    fontStyle = FontStyle.Normal,
                    fontWeight = FontWeight.Medium,
                    fontFamily = FontFamily.SansSerif,
                )
            }
        },
        containerColor = LocalChatAppColorScheme.current.othersMessageBackground,
    ) {
        Text(
            text = snackbarData.visuals.message,
            color = LocalChatAppColorScheme.current.othersMessageContent,
            fontSize = 14.sp,
            fontStyle = FontStyle.Normal,
            fontWeight = FontWeight.Normal,
            fontFamily = FontFamily.SansSerif
        )
    }
}

@Composable
fun ChatAppInfoSnackbar(snackbarData: SnackbarData) {
    Snackbar(
        containerColor = LocalChatAppColorScheme.current.othersMessageBackground,
    ) {
        Text(
            text = snackbarData.visuals.message,
            color = LocalChatAppColorScheme.current.othersMessageContent,
            fontSize = 14.sp,
            fontStyle = FontStyle.Normal,
            fontWeight = FontWeight.Normal,
            fontFamily = FontFamily.SansSerif
        )
    }
}
