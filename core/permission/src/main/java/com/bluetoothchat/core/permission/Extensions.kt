package com.bluetoothchat.core.permission

import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.isGranted

@OptIn(ExperimentalPermissionsApi::class)
fun MultiplePermissionsState.isGranted() = permissions.all { it.status.isGranted }

@OptIn(ExperimentalPermissionsApi::class)
fun PermissionState.isGranted() = status.isGranted
