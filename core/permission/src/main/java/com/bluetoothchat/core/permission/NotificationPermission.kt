package com.bluetoothchat.core.permission

import android.Manifest
import android.app.Activity
import android.os.Build
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.rememberPermissionState

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun rememberNotificationPermissionState(onStatusChanged: (PermissionStatus) -> Unit): PermissionState? =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val activity = LocalContext.current as Activity
        val permission = remember { Manifest.permission.POST_NOTIFICATIONS }
        rememberPermissionState(
            permission = Manifest.permission.POST_NOTIFICATIONS,
            onPermissionResult = { granted ->
                Log.v("NotificationPermission", "Notification Permissions result $granted")

                val permissionStatus = if (granted) {
                    PermissionStatus.Granted(isInitial = false)
                } else {
                    val shouldShowRationale = ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)
                    PermissionStatus.Denied(isInitial = false, shouldShowRationale = shouldShowRationale)
                }
                onStatusChanged(permissionStatus)
            },
        )
    } else {
        null
    }
