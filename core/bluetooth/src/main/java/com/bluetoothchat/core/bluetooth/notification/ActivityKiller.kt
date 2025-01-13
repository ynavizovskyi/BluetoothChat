package com.bluetoothchat.core.bluetooth.notification

import android.app.Activity

interface ActivityKiller {

    fun isKilled(): Boolean

    fun setActivity(activity: Activity)

    fun kill()

}
