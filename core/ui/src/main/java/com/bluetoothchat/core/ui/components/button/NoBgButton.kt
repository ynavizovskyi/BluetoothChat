package com.bluetoothchat.core.ui.components.button

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.bluetoothchat.core.ui.theme.LocalChatAppColorScheme

@Composable
fun NoBgButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    contentPadding: PaddingValues = defaultCustomButtonPaddings(),
    colors: CustomButtonColors = defaultNoBgButtonColors(),
) {
    CustomButton(
        modifier = modifier,
        onClick = onClick,
        enabled = enabled,
        colors = colors,
        contentPadding = contentPadding,
    ) {
        Text(text = text, style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Medium))
    }
}

@Composable
fun defaultNoBgButtonColors(
    contentColor: Color = LocalChatAppColorScheme.current.accent,
    disabledContentColor: Color = LocalChatAppColorScheme.current.onScreenBackground.copy(alpha = 0.5f),
): CustomButtonColors = defaultButtonColors(
    containerColor = Color.Transparent,
    contentColor = contentColor,
    disabledContainerColor = Color.Transparent,
    disabledContentColor = disabledContentColor,
)
