package com.bluetoothchat.app.session

import androidx.compose.ui.graphics.toArgb
import com.bluetoothchat.core.session.SessionUserColorProvider
import com.bluetoothchat.core.ui.theme.Colors

class SessionUserColorProviderImpl : SessionUserColorProvider {

    override fun getArgbColors(): List<Int> = Colors.UserColors.map { it.toArgb() }

}
