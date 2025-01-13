package com.bluetoothchat.core.bluetooth.message.model

sealed interface BtParseMessageResult {
    data class Success(val message: Protocol) : BtParseMessageResult

    sealed interface Error : BtParseMessageResult {
        data class IncompatibleProtocols(val myProtocolVersion: Int, val theirProtocolVersion: Int) : Error
    }
}
