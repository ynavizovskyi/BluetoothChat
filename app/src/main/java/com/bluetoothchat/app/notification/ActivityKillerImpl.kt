package com.bluetoothchat.app.notification

import android.app.Activity
import com.bluetoothchat.core.bluetooth.notification.ActivityKiller
import java.lang.ref.WeakReference
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ActivityKillerImpl @Inject constructor() : ActivityKiller {

    private var activity: WeakReference<Activity?> = WeakReference(null)

    override fun isKilled(): Boolean = activity.get() == null

    override fun setActivity(activity: Activity) {
        this.activity = WeakReference(activity)
    }

    override fun kill() {
        activity.get()?.finishAndRemoveTask()
        activity = WeakReference(null)
    }

}
