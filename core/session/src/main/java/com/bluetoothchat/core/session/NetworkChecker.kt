package com.bluetoothchat.core.session

import android.content.Context
import android.net.ConnectivityManager

class NetworkChecker(
    private val context: Context,
) {

    fun isConnected(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val info = connectivityManager.activeNetworkInfo
        return info?.let { it.isConnectedOrConnecting || it.isAvailable } ?: false
    }

}
