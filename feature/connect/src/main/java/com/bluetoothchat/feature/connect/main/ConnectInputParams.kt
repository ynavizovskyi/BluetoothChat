package com.bluetoothchat.feature.connect.main

import kotlinx.serialization.Serializable

@Serializable
data class ConnectInputParams(val startScanningOnStart: Boolean, val source: String)
