package com.bluetoothchat.app.notification

import android.app.Activity
import com.bluetoothchat.core.bluetooth.notification.ActivityKiller
import java.lang.ref.WeakReference

class ActivityKillerImpl : ActivityKiller {

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
