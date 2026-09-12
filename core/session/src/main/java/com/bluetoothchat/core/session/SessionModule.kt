package com.bluetoothchat.core.session

import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val coreSessionModule = module {
    singleOf(::SessionImpl) bind Session::class
    singleOf(::NetworkChecker)
}
