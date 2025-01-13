package com.bluetoothchat.core.analytics

import com.bluetoothchat.core.domain.model.Chat
import com.bluetoothchat.core.prefs.settings.model.ChatAppTheme

fun ChatAppTheme.toAnalyticsValue() = when (this) {
    ChatAppTheme.SYSTEM -> "system"
    ChatAppTheme.LIGHT -> "light"
    ChatAppTheme.DARK -> "dark"
}

fun Chat.Group.toPropertyMap() = mapOf("member_count" to users.size)
