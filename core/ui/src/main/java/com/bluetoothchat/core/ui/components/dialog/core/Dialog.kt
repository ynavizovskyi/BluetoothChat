package com.bluetoothchat.core.ui.components.dialog.core

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.bluetoothchat.core.ui.theme.LocalChatAppColorScheme

private val DialogMinWidth = 280.dp
private val DialogMaxWidth = 320.dp
private val DialogElevation = 6.dp
private val DialogCornerRadius = 24.dp
private val DialogPadding = PaddingValues(all = 24.dp)
private val TitlePadding = PaddingValues(bottom = 16.dp)
private val ContentPadding = PaddingValues(bottom = 24.dp)
private val ButtonsMainAxisSpacing = 8.dp
private val ButtonsCrossAxisSpacing = 12.dp

@Composable
internal fun Dialog(
    title: @Composable () -> Unit,
    confirmButton: @Composable () -> Unit,
    dismissButton: @Composable (() -> Unit)? = null,
    onDismissRequest: () -> Unit,
    content: @Composable () -> Unit
) {
    DialogContainer(onDismissRequest = onDismissRequest) {
        Box(
            modifier = Modifier
                .padding(TitlePadding),
        ) { title() }

        Box(
            modifier = Modifier
                .padding(ContentPadding),
        ) { content() }

        Box(modifier = Modifier.align(Alignment.End)) {
            DialogFlowRow(
                mainAxisSpacing = ButtonsMainAxisSpacing,
                crossAxisSpacing = ButtonsCrossAxisSpacing
            ) {
                dismissButton?.invoke()
                confirmButton()
            }
        }
    }
}

@Composable
internal fun DialogContainer(
    onDismissRequest: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    androidx.compose.ui.window.Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            modifier = Modifier
                .sizeIn(minWidth = DialogMinWidth, maxWidth = DialogMaxWidth),
            shape = RoundedCornerShape(DialogCornerRadius),
            color = LocalChatAppColorScheme.current.dialogBackground,
            contentColor = LocalChatAppColorScheme.current.dialogContent,
            tonalElevation = DialogElevation,
        ) {
            Column(
                modifier = Modifier.padding(DialogPadding)
            ) {
                content()
            }
        }
    }
}
