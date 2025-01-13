package com.bluetoothchat.core.ui.components.button

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalMinimumInteractiveComponentEnforcement
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.bluetoothchat.core.ui.theme.LocalChatAppColorScheme

private val CustomButtonDefaultPaddings = PaddingValues(horizontal = 24.dp, vertical = 8.dp)

@Composable
fun defaultCustomButtonPaddings(
    horizontal: Dp = 24.dp,
    vertical: Dp = 8.dp,
): PaddingValues {
    return PaddingValues(horizontal = horizontal, vertical = vertical)
}

@Composable
fun CustomButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    CustomButton(modifier = modifier, onClick = onClick, enabled = enabled) {
        Text(text = text)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = ButtonDefaults.shape,
    colors: CustomButtonColors = defaultButtonColors(),
    border: BorderStroke? = null,
    contentPadding: PaddingValues = CustomButtonDefaultPaddings,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    content: @Composable RowScope.() -> Unit
) {
    val containerColor = colors.containerColor(enabled).value
    val contentColor = colors.contentColor(enabled).value
    CompositionLocalProvider(LocalMinimumInteractiveComponentEnforcement provides false) {
        Surface(
            onClick = onClick,
            modifier = modifier.semantics { role = Role.Button },
            enabled = enabled,
            shape = shape,
            color = containerColor,
            contentColor = contentColor,
            border = border,
            interactionSource = interactionSource
        ) {
            CompositionLocalProvider(LocalContentColor provides contentColor) {
                Row(
                    Modifier
                        .padding(contentPadding),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    content = content
                )
            }
        }
    }
}

@Composable
fun defaultButtonColors(
    containerColor: Color = LocalChatAppColorScheme.current.accent,
    contentColor: Color = LocalChatAppColorScheme.current.onAccent,
    disabledContainerColor: Color = Color.Gray,
    disabledContentColor: Color = Color.White.copy(alpha = 0.5f),
): CustomButtonColors = CustomButtonColors(
    containerColor = containerColor,
    contentColor = contentColor,
    disabledContainerColor = disabledContainerColor,
    disabledContentColor = disabledContentColor
)
