package com.bluetoothchat.core.bluetooth

import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.ServiceInfo
import android.os.Binder
import android.os.Build
import android.os.IBinder
import com.bluetoothchat.core.bluetooth.message.manager.CommunicationManagerImpl
import com.bluetoothchat.core.bluetooth.notification.ActivityKiller
import com.bluetoothchat.core.bluetooth.notification.FOREGROUND_SERVICE_NOTIFICATION_ID
import com.bluetoothchat.core.bluetooth.notification.NotificationManagerWrapper
import com.bluetoothchat.core.dispatcher.ApplicationScope
import com.bluetoothchat.core.dispatcher.DispatcherManager
import com.bluetoothchat.core.permission.PermissionManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

internal const val ACTION_STOP = "action.stop"

@AndroidEntryPoint
class BtService : Service() {

    @Inject
    lateinit var applicationScope: ApplicationScope

    @Inject
    lateinit var dispatcherManager: DispatcherManager

    @Inject
    lateinit var communicationManager: CommunicationManagerImpl

    @Inject
    lateinit var permissionManager: PermissionManager

    @Inject
    lateinit var notificationManager: NotificationManagerWrapper

    @Inject
    lateinit var activityKiller: ActivityKiller

    override fun onCreate() {
        super.onCreate()
        when {
            !permissionManager.bluetoothPermissionsGranted() -> {
                stopSelf()
            }

            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
                startForeground(
                    FOREGROUND_SERVICE_NOTIFICATION_ID,
                    notificationManager.getForegroundNotification(),
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE
                )
            }

            else -> {
                startForeground(FOREGROUND_SERVICE_NOTIFICATION_ID, notificationManager.getForegroundNotification())
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            applicationScope.launch(dispatcherManager.default) {
                activityKiller.kill()
                communicationManager.disconnectAll()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()

                //This seems to be the only way to kill the app completely
                //And prevent others from being able to connect
                android.os.Process.killProcess(android.os.Process.myPid())
            }
        }
        //This means that this service won't be restarted after the process is killed
        //It prevents crashes on Android 14+
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder {
        return ConnectionBinder()
    }

    suspend fun ensureStarted(){
        communicationManager.ensureStarted()
    }


    inner class ConnectionBinder : Binder() {
        fun getService() = this@BtService
    }

    companion object {
        fun start(context: Context) {
            val intent = Intent(context, BtService::class.java)
            context.startForegroundService(intent)
        }

        fun bind(context: Context, connection: ServiceConnection) {
            val intent = Intent(context, BtService::class.java)
            context.bindService(intent, connection, Context.BIND_ABOVE_CLIENT)
        }
    }
}
