package com.bluetoothchat.app

import android.app.Application
import android.util.Log
import com.bluetoothchat.core.bluetooth.BtServiceManager
import com.bluetoothchat.core.session.Session
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class BluetoothChatApp : Application() {

    @Inject
    lateinit var session: Session

    @Inject
    lateinit var btServiceManager: BtServiceManager

    override fun onCreate() {
        super.onCreate()
        //Initializing dependencies that should exist from the very start of the app
        session.init()
    }

}
