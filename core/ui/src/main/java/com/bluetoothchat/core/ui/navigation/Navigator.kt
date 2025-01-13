package com.bluetoothchat.core.ui.navigation

import androidx.compose.runtime.Composable
import com.bluetoothchat.core.ui.components.dialog.model.DialogInputParams
import com.bluetoothchat.core.ui.components.dialog.model.DialogResult

interface Navigator {

    fun navigateBack()

    fun showDialog(params: DialogInputParams)

    @Composable
    fun OnDialogResult(callback: (DialogResult) -> Unit)

}
