package com.bluetoothchat.core.filemanager

import android.net.Uri

interface ApkExtractor {

    suspend fun extractApk(): Uri?

}
