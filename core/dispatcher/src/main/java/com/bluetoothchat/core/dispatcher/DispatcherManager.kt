package com.bluetoothchat.core.dispatcher

import kotlinx.coroutines.Dispatchers
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DispatcherManager @Inject constructor() {
    val main = Dispatchers.Main
    val io = Dispatchers.IO
    val default = Dispatchers.Default
}
