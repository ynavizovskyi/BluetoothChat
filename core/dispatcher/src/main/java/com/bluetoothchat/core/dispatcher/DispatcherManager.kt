package com.bluetoothchat.core.dispatcher

import kotlinx.coroutines.Dispatchers

class DispatcherManager {
    val main = Dispatchers.Main
    val io = Dispatchers.IO
    val default = Dispatchers.Default
}
