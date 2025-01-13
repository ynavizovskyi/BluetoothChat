package com.bluetoothchat.core.ui.model

import com.bluetoothchat.core.bluetooth.message.ConnectionState
import com.bluetoothchat.core.bluetooth.scanner.BtDevice

data class ViewBtDevice(val device: BtDevice, val connectionState: ConnectionState)
