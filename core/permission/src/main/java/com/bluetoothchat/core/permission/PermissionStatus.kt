package com.bluetoothchat.core.permission

sealed interface PermissionStatus {
    val isInitial: Boolean

    data class Granted(override val isInitial: Boolean) : PermissionStatus
    data class Denied(override val isInitial: Boolean, val shouldShowRationale: Boolean) : PermissionStatus

}

fun PermissionStatus.isGranted() = this is PermissionStatus.Granted
