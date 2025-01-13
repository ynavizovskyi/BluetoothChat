package com.bluetoothchat.core.bluetooth.connection

import com.bluetoothchat.core.bluetooth.message.model.BtParseMessageResult

data class BtMessageWithAddress(val message: BtParseMessageResult, val deviceAddress: String)
