package com.bluetoothchat.app

import android.content.Context

//This object purpose is to exclude Debug Menu from the release build
//Each of the build types contains a copy of it
object DebugMenuLauncher {

    const val isAvailable = false

    fun launch(context: Context) {
        throw IllegalAccessException("Debug menu is not available in release build")
    }

}
