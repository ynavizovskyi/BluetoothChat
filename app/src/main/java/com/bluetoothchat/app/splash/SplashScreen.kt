package com.bluetoothchat.app.splash

import androidx.compose.runtime.Composable
import androidx.lifecycle.Lifecycle
import com.bluetoothchat.core.ui.util.observeWithLifecycle
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SplashScreen() {
    val navigator = LocalSplashScreenNavigator.current
    val viewModel: SplashScreenViewModel = koinViewModel()

    viewModel.oneTimeEvent.observeWithLifecycle(minActiveState = Lifecycle.State.RESUMED) { event ->
        when (event) {
            is SplashScreenEvent.NavigateToMain -> navigator.navigateToMain()
            is SplashScreenEvent.NavigateToProfileSetUpScreen -> navigator.navigateToMyProfile()
        }
    }
}
