package com.bluetoothchat.app.navigation

import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey

internal inline fun <reified T> NavBackStack<NavKey>.clearUpTo(keepTopElement: Boolean = true) {
    val mainIndex = indexOfLast { it is T }
    if (mainIndex == -1) return

    while (size > mainIndex + 1 + if (keepTopElement) 1 else 0) {
        removeAt(lastIndex - if (keepTopElement) 1 else 0)
    }
}
