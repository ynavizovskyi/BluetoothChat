package com.bluetoothchat.core.permission

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

class PermissionManager(
    private val context: Context,
) {

    fun bluetoothPermissionsGranted(): Boolean {
        return getBluetoothPermissions().permissions.map { permission ->
            ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
        }.all { it }
    }

    fun writeExternalStoragePermissionGranted(): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                PackageManager.PERMISSION_GRANTED


}
