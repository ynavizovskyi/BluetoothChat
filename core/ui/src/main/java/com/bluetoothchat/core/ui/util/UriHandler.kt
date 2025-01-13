package com.bluetoothchat.core.ui.util

import android.content.ActivityNotFoundException
import androidx.compose.ui.platform.UriHandler

fun UriHandler.safeOpenUri(uri: String, completeListener: (success: Boolean) -> Unit = {}) {
    try {
        this.openUri(uri)
        completeListener(true)
    } catch (e: ActivityNotFoundException) {
        completeListener(false)
    }
}
