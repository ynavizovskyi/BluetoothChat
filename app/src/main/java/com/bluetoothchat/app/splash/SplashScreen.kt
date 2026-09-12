package com.bluetoothchat.app.splash

import androidx.compose.runtime.Composable
import androidx.lifecycle.Lifecycle
import com.bluetoothchat.core.ui.util.observeWithLifecycle
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.spec.DestinationStyle
import org.koin.compose.viewmodel.koinViewModel

@Destination(start = true, style = DestinationStyle.Animated.None::class)
@Composable
fun SplashScreen(navigator: SplashScreenNavigatorImpl) {
    val viewModel: SplashScreenViewModel = koinViewModel()

    viewModel.oneTimeEvent.observeWithLifecycle(minActiveState = Lifecycle.State.RESUMED) { event ->
        when (event) {
            is SplashScreenEvent.NavigateToMain -> navigator.navigateToMain()
            is SplashScreenEvent.NavigateToProfileSetUpScreen -> navigator.navigateToMyProfile()
        }
    }

}
