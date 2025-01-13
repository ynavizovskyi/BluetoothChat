package com.bluetoothchat.core.bluetooth

import android.content.ComponentName
import android.content.Context
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import com.bluetoothchat.core.dispatcher.ApplicationScope
import com.bluetoothchat.core.dispatcher.DispatcherManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@Singleton
class BtServiceManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val applicationScope: ApplicationScope,
    private val dispatcherManager: DispatcherManager,
) {

    private var service: BtService? = null

    private var connectListener: () -> Unit = {}

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, binder: IBinder) {
            service = (binder as BtService.ConnectionBinder).getService()
            connectListener.invoke()
        }

        override fun onServiceDisconnected(className: ComponentName) {
            service = null
        }
    }

    fun ensureStarted() {
        applicationScope.launch(dispatcherManager.io) {
            suspendCoroutine { continuation ->
                if (!isStarted()) {
                    startService(
                        listener = {
                            Log.v("BtServiceManager", "BtService started")
                            continuation.resume(Unit)
                        },
                    )
                } else {
                    continuation.resume(Unit)
                }
            }

            service?.ensureStarted()
        }
    }

    private fun startService(listener: () -> Unit) {
        this.connectListener = listener
        BtService.start(context)
        BtService.bind(context, serviceConnection)
    }

    private fun isStarted() = service != null

}
