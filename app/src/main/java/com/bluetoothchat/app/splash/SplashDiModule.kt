package com.bluetoothchat.app.splash

import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val splashModule = module {
    factoryOf(::SplashScreenViewModel)
}
