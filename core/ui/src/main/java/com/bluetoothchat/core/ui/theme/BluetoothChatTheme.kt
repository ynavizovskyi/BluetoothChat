package com.bluetoothchat.core.ui.theme

import android.app.Activity
import android.view.View
import android.view.Window
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.Dp
import androidx.core.view.WindowCompat

@Composable
fun ChatAppTheme(
    darkTheme: Boolean,
    content: @Composable () -> Unit,
) {
    val colorScheme = remember(darkTheme) {
        if (darkTheme) darkChatAppColorScheme() else lightChatAppColorScheme()
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        val window = (view.context as Activity).window
        val localColorScheme = LocalChatAppColorScheme.current
        DisposableEffect(darkTheme) {
            setWindowColors(window = window, view = view, colorScheme = colorScheme)
            //This is needed to reset the window properties to the global theme values
            //once the scope is close when theme used as a local wrapper to override global values
            onDispose { setWindowColors(window = window, view = view, colorScheme = localColorScheme) }
        }
    }

    val customTextSelectionColors = TextSelectionColors(
        handleColor = colorScheme.accent,
        backgroundColor = colorScheme.accent.copy(alpha = 0.4f)
    )

    CompositionLocalProvider(
        LocalChatAppColorScheme provides colorScheme,
        LocalIndication provides rememberRippleIndicator(darkTheme = darkTheme),
        LocalTextSelectionColors provides customTextSelectionColors
    ) {
        content()
    }
}

private fun setWindowColors(window: Window, view: View, colorScheme: ChatAppColorScheme){
    window.navigationBarColor = Color.Transparent.toArgb()
    window.statusBarColor = Color.Transparent.toArgb()

    //Status bar should be light on both black and blue status bars
    WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
    WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !colorScheme.isDark
}

@Composable
private fun rememberRippleIndicator(darkTheme: Boolean) = remember(darkTheme) {
    if (darkTheme) {
        ripple(radius = Dp.Unspecified, color = Color.White)
    } else {
        ripple(radius = Dp.Unspecified, color = Color.Black)
    }
}
