package com.bluetoothchat.core.ui.util

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable

fun createEnableBluetoothIntent(): Intent {
    return Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
}

@Composable
fun rememberLauncherEnableBluetooth(onResult: (Boolean) -> Unit): ActivityResultLauncher<Intent> {
    return rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        onResult(result.resultCode == Activity.RESULT_OK)
    }
}
