package com.bluetoothchat.core.ui.model.mapper

import com.bluetoothchat.core.bluetooth.message.ConnectionState
import com.bluetoothchat.core.bluetooth.scanner.BtDevice
import com.bluetoothchat.core.ui.model.ViewBtDevice
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ViewBtDeviceMapper @Inject constructor() {

    suspend fun map(
        device: BtDevice,
        connectionsStates: Map<String, ConnectionState>,
    ): ViewBtDevice {
        return ViewBtDevice(
            device = device,
            connectionState = connectionsStates[device.address] ?: ConnectionState.DISCONNECTED
        )
    }

}
