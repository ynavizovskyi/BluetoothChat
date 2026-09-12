package com.bluetoothchat.core.dispatcher

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

class ApplicationScope(dispatchersProvider: DispatcherManager) :
    CoroutineScope by CoroutineScope(SupervisorJob() + dispatchersProvider.default)
