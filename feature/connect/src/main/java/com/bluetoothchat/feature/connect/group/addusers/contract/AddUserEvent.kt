package com.bluetoothchat.feature.connect.group.addusers.contract

import com.bluetoothchat.core.ui.components.dialog.model.DialogInputParams
import com.bluetoothchat.core.ui.mvi.contract.ViewOneTimeEvent

internal sealed interface AddUserEvent : ViewOneTimeEvent {

    object NavigateBack : AddUserEvent

    object RequestBluetoothPermission : AddUserEvent

    object RequestEnabledBluetooth : AddUserEvent

    data class ShowErrorDialog(val params: DialogInputParams) : AddUserEvent


}
