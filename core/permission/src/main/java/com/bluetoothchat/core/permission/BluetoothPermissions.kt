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
import com.google.accompanist.permissions.MultiplePermissionsState
import com.google.accompanist.permissions.rememberMultiplePermissionsState

data class BluetoothPermissions(
    val permissions: List<String>,
    val permissionType: BluetoothPermissionType,
)

enum class BluetoothPermissionType { LOCATION, BLUETOOTH }

fun getBluetoothPermissions() = when {
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> BluetoothPermissions(
        permissions = listOf(
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_ADVERTISE,
            Manifest.permission.BLUETOOTH_CONNECT,
        ),
        permissionType = BluetoothPermissionType.BLUETOOTH,
    )

    Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> BluetoothPermissions(
        permissions = listOf(Manifest.permission.ACCESS_FINE_LOCATION),
        permissionType = BluetoothPermissionType.LOCATION,
    )

    else -> BluetoothPermissions(
        permissions = listOf(Manifest.permission.ACCESS_COARSE_LOCATION),
        permissionType = BluetoothPermissionType.LOCATION,
    )
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun rememberBluetoothPermissionsState(
    onStatusChanged: (PermissionStatus) -> Unit,
): MultiplePermissionsState {
    val permissions = remember { getBluetoothPermissions() }
    return rememberBluetoothPermissionsState(bluetoothPermissions = permissions, onStatusChanged = onStatusChanged)
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun rememberBluetoothPermissionsState(
    bluetoothPermissions: BluetoothPermissions,
    onStatusChanged: (PermissionStatus) -> Unit,
): MultiplePermissionsState {
    val activity = LocalContext.current as Activity

    return rememberMultiplePermissionsState(
        permissions = bluetoothPermissions.permissions,
        onPermissionsResult = { result ->
            Log.v("BluetoothPermissions", "Permissions result $result")
            val granted = result.isNotEmpty() && result.all { it.value }
            val permissionStatus = if (granted) {
                PermissionStatus.Granted(isInitial = false)
            } else {
                val shouldShowRationale = result.any {
                    !ActivityCompat.shouldShowRequestPermissionRationale(activity, it.key)
                }
                PermissionStatus.Denied(isInitial = false, shouldShowRationale = shouldShowRationale)
            }

            onStatusChanged(permissionStatus)
        }
    )
}
