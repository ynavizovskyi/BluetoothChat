package com.bluetoothchat.app

import android.app.Application
import android.content.Context
import com.bluetoothchat.core.session.Session
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.dsl.module

class BluetoothChatApp : Application(), KoinComponent {

    private val session: Session by inject()

    override fun onCreate() {
        super.onCreate()

        initKoin(
            appModule = module {
                single<Context> { this@BluetoothChatApp }
            }
        )

        //Initializing dependencies that should exist from the very start of the app
        session.init()
    }

}
