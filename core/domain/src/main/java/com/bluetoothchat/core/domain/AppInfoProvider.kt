package com.bluetoothchat.core.domain

interface AppInfoProvider {

    fun getAppName() : String

    fun getDebugMenuAvailable(): Boolean

    fun getAppVersion(): String

    fun getProtocolVersion(): String

}
