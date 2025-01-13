package com.bluetoothchat.core.ui.util

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable

fun createPickImageIntent(): Intent {
    val MimeTypeImages = arrayOf(
        "image/jpg", "image/jpeg", "image/png", "image/heic",
        "image/heif", "image/webp", "image/vnd.wap.wbmp"
    )
    return Intent(Intent.ACTION_GET_CONTENT)
        .setType("image/*")
        .putExtra(
            Intent.EXTRA_MIME_TYPES,
            MimeTypeImages
        )
}

@Composable
fun rememberLauncherForImage(onResult: (Uri) -> Unit): ActivityResultLauncher<Intent> {
    return rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                onResult(uri)
            }
        } else {

        }
    }
}
