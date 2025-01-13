package com.bluetoothchat.core.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

sealed interface Chat : Parcelable {

    val id: String
    val createdTimestamp: Long
    val exists: Boolean
    val name: String

    @Parcelize
    data class Private(
        override val createdTimestamp: Long,
        override val exists: Boolean,
        val user: User,
    ) : Chat {
        override val id get() = user.deviceAddress
        override val name get() = user.userName ?: user.deviceAddress
    }

    @Parcelize
    data class Group(
        override val id: String,
        override val createdTimestamp: Long,
        override val exists: Boolean,
        override val name: String,
        val hostDeviceAddress: String,
        val picture: Picture?,
        val users: List<User>,
    ) : Chat

}
