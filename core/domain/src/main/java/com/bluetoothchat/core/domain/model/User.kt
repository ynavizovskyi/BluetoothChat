package com.bluetoothchat.core.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class User(
    val deviceAddress: String,
    val color: Int,
    val deviceName: String?,
    val userName: String?,
    val picture: Picture?,
) : Parcelable

fun User.isSetUp() = userName != null
