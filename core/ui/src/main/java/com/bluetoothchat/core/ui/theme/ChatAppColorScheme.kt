package com.bluetoothchat.core.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

val LocalChatAppColorScheme = staticCompositionLocalOf { darkChatAppColorScheme() }

class ChatAppColorScheme(
    val isDark: Boolean,
    val navigationBar: Color,
    val toolbar: Color,
    val onToolbar: Color,
    val screenBackground: Color,
    val onScreenBackground: Color,
    val chatScreenBackground: Color,
    val chatInputBackground: Color,
    val accent: Color,
    val accentVariant: Color,
    val onAccent: Color,
    val secondary: Color,
    val alert: Color,
    val mineMessageBackground: Color,
    val mineMessageContent: Color,
    val othersMessageBackground: Color,
    val othersMessageContent: Color,
)

fun darkChatAppColorScheme() = ChatAppColorScheme(
    isDark = true,
    navigationBar = Colors.Ebony,
    toolbar = Colors.EbonyClay,
    onToolbar = Colors.White,
    screenBackground = Colors.Bunker,
    onScreenBackground = Colors.White,
    chatScreenBackground = Colors.Firefly,
    chatInputBackground = Colors.EbonyClay,
    accent = Colors.Lochmara,
    accentVariant = Colors.SurfieGreen,
    onAccent = Colors.White,
    secondary = Colors.TreePoppy,
    alert = Colors.Cinnabar,
    mineMessageBackground = Colors.EveningSea,
    mineMessageContent = Colors.White,
    othersMessageBackground = Colors.EbonyClay,
    othersMessageContent = Colors.White,
)

fun lightChatAppColorScheme() = ChatAppColorScheme(
    isDark = false,
    navigationBar = Colors.AthensGray,
    toolbar = Colors.SurfieGreen,
    onToolbar = Colors.White,
    screenBackground = Colors.White,
    onScreenBackground = Colors.Black,
    chatScreenBackground = Colors.DawnPink,
    chatInputBackground = Colors.White,
    accent = Colors.Lochmara,
    accentVariant = Colors.SurfieGreen,
    onAccent = Colors.White,
    secondary = Colors.TreePoppy,
    alert = Colors.Cinnabar,
    mineMessageBackground = Colors.RiceFlower,
    mineMessageContent = Colors.Black,
    othersMessageBackground = Colors.White,
    othersMessageContent = Colors.Black,
)

val ChatAppColorScheme.mainItemDivider get() = onScreenBackground.copy(alpha = 0.05f)
val ChatAppColorScheme.mainItemMessageText get() = onScreenBackground.copy(alpha = 0.5f)
val ChatAppColorScheme.mainItemTimeText get() = onScreenBackground.copy(alpha = 0.5f)
val ChatAppColorScheme.settingsSectionDivider get() = onScreenBackground.copy(alpha = 0.05f)
val ChatAppColorScheme.onBackgroundInfoText get() = onScreenBackground.copy(alpha = 0.75f)
val ChatAppColorScheme.onScreenBackground75 get() = onScreenBackground.copy(alpha = 0.75f)
