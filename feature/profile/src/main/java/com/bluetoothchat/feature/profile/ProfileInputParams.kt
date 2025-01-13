package com.bluetoothchat.feature.profile

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

data class ProfileInputParams(val mode: ProfileLaunchMode, val source: String)

sealed interface ProfileLaunchMode : Parcelable {

    @Parcelize
    data class Me(val isInitialSetUp: Boolean) : ProfileLaunchMode

    @Parcelize
    data class Other(val userDeviceAddress: String) : ProfileLaunchMode

}
