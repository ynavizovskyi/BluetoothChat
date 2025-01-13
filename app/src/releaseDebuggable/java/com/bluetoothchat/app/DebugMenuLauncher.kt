package com.bluetoothchat.app

import android.content.Context

//This object purpose is to exclude Debug Menu from the release build
//Each of the build types contains a copy of it
object DebugMenuLauncher {

    const val isAvailable = true

    fun launch(context: Context) {
//        DebugMenuActivity.launch(context)
    }

}
