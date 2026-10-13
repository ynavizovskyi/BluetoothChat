package com.bluetoothchat.feature.profile

import kotlinx.serialization.Serializable

@Serializable
data class ProfileInputParams(val mode: ProfileLaunchMode, val source: String)

@Serializable
sealed interface ProfileLaunchMode {

    @Serializable
    data class Me(val isInitialSetUp: Boolean) : ProfileLaunchMode

    @Serializable
    data class Other(val userDeviceAddress: String) : ProfileLaunchMode

}
