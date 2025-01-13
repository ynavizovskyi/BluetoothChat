package com.bluetoothchat.core.dispatcher

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ApplicationScope @Inject constructor(dispatchersProvider: DispatcherManager) :
    CoroutineScope by CoroutineScope(SupervisorJob() + dispatchersProvider.default)
