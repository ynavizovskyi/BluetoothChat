package com.bluetoothchat.core.ui.navigation

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.bluetoothchat.core.ui.components.dialog.model.DialogInputParams
import com.bluetoothchat.core.ui.components.dialog.model.DialogResult
import com.bluetoothchat.core.ui.destinations.DialogDestination
import com.ramcosta.composedestinations.navigation.navigate
import com.ramcosta.composedestinations.result.NavResult
import com.ramcosta.composedestinations.result.OpenResultRecipient

open class BaseNavigator(
    protected val navController: NavController,
    private val dialogResultRecipient: OpenResultRecipient<DialogResult>,
) : Navigator {

    override fun navigateBack() {
        //When destination was not popped it is because it is the last one (or I hope so)
        //And we should finish activity
        val popped = navController.popBackStack()
        if (!popped) {
            (navController.context as Activity).finish()
        }
    }

    override fun showDialog(params: DialogInputParams) {
        navController.navigate(direction = DialogDestination(params))
    }

    @Composable
    override fun OnDialogResult(callback: (DialogResult) -> Unit) {
        dialogResultRecipient.onNavResult { navResult ->
            when (navResult) {
                is NavResult.Value -> callback(navResult.value)
                else -> Unit
            }
        }
    }

}
