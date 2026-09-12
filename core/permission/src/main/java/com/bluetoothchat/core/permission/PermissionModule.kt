package com.bluetoothchat.core.permission

import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val corePermissionModule = module {
    singleOf(::PermissionManager)
}
