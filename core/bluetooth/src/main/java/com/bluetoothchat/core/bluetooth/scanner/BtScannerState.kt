package com.bluetoothchat.core.bluetooth.scanner

import android.bluetooth.BluetoothDevice

data class BtScannerState(
    val isDiscoverable: Boolean,
    val isScanning: Boolean,
    val foundDevices: List<BtDevice>,
    val pairedDevices: List<BtDevice>,
)

internal data class BtScannerStateInternal(
    val isDiscoverable: Boolean,
    val isScanning: Boolean,
    val foundDevices: List<BluetoothDevice>,
    val pairedDevices: List<BluetoothDevice>,
)
