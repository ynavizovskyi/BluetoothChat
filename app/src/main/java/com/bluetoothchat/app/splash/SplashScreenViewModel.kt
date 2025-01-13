package com.bluetoothchat.app.splash

import androidx.lifecycle.viewModelScope
import com.bluetoothchat.core.dispatcher.DispatcherManager
import com.bluetoothchat.core.domain.model.isSetUp
import com.bluetoothchat.core.session.Session
import com.bluetoothchat.core.ui.mvi.MviViewModel
import com.bluetoothchat.core.ui.mvi.contract.ViewAction
import com.bluetoothchat.core.ui.mvi.contract.ViewState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class SplashScreenViewModel @Inject constructor(
    private val session: Session,
    private val dispatcherManager: DispatcherManager,
) : MviViewModel<ViewState, ViewAction, SplashScreenEvent>(SplashScreenState) {

    init {
        redirectToStartDestination()
    }

    //Nothing to handle here
    override fun handleAction(action: ViewAction) = Unit

    private fun redirectToStartDestination() {
        viewModelScope.launch(dispatcherManager.default) {
            if (session.getUser().isSetUp()) {
                sendEvent { SplashScreenEvent.NavigateToMain }
            } else {
                sendEvent { SplashScreenEvent.NavigateToProfileSetUpScreen }
            }
        }
    }

}
