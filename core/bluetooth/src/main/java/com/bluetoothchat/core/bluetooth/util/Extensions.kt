package com.bluetoothchat.core.bluetooth.util

import android.bluetooth.BluetoothClass
import android.bluetooth.BluetoothDevice

fun BluetoothDevice.isPhone() = this.bluetoothClass.majorDeviceClass == BluetoothClass.Device.Major.PHONE
