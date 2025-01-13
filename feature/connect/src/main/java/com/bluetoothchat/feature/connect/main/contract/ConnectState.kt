package com.bluetoothchat.feature.connect.main.contract

import com.bluetoothchat.core.ui.model.ViewBtDeviceWithUser
import com.bluetoothchat.core.ui.mvi.contract.ViewState

internal sealed interface ConnectState : ViewState {

    object None : ConnectState

    object NoBluetoothPermission : ConnectState

    object BluetoothDisabled : ConnectState

    data class Discovering(
        val foundDevices: List<ViewBtDeviceWithUser>,
        val pairedDevices: List<ViewBtDeviceWithUser>,
        val isDeviceDiscoverable: Boolean,
        val isScanning: Boolean,
        val displayProgressOverlay: Boolean,
    ) : ConnectState

}
