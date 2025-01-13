package com.bluetoothchat.core.ui.util

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import com.bluetoothchat.core.ui.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

fun SnackbarHostState.showGrantPermissionSnackbar(
    context: Context,
    coroutineScope: CoroutineScope,
    @StringRes messageResId: Int,
    onActionClicked: () -> Unit,
    onDismissed: () -> Unit = {},
) {
    coroutineScope.launch {
        val result = this@showGrantPermissionSnackbar.showSnackbar(
            message = context.getString(messageResId),
            actionLabel = context.getString(R.string.screen_title_settings),
            duration = SnackbarDuration.Long
        )
        when (result) {
            SnackbarResult.Dismissed -> onDismissed()
            SnackbarResult.ActionPerformed -> onActionClicked()
        }
    }
}

fun SnackbarHostState.showImageSavedSnackbar(
    context: Context,
    coroutineScope: CoroutineScope,
    onDismissed: () -> Unit = {},
) {
    coroutineScope.launch {
        val result = this@showImageSavedSnackbar.showSnackbar(
            message = context.getString(R.string.snackbar_saved_to_gallery),
            duration = SnackbarDuration.Short
        )
        when (result) {
            SnackbarResult.Dismissed -> onDismissed()
            SnackbarResult.ActionPerformed -> Unit
        }
    }
}


fun SnackbarHostState.showTextCopiedSnackbar(
    context: Context,
    coroutineScope: CoroutineScope,
    onDismissed: () -> Unit = {},
) {
    coroutineScope.launch {
        val result = this@showTextCopiedSnackbar.showSnackbar(
            message = context.getString(R.string.snackbar_message_copied),
            duration = SnackbarDuration.Short
        )
        when (result) {
            SnackbarResult.Dismissed -> onDismissed()
            SnackbarResult.ActionPerformed -> Unit
        }
    }
}
