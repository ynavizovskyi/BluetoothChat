package com.bluetoothchat.core.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
class Picture(val id: String, val sizeBytes: Long) : Parcelable
