package com.bluetoothchat.core.ui.mvi

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bluetoothchat.core.ui.mvi.contract.ViewAction
import com.bluetoothchat.core.ui.mvi.contract.ViewOneTimeEvent
import com.bluetoothchat.core.ui.mvi.contract.ViewState
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

abstract class MviViewModel<State : ViewState, Action : ViewAction, OneTimeEvent : ViewOneTimeEvent>
    (initialState: State, private val savedStateHandle: SavedStateHandle? = null) : ViewModel() {

    private val stateKey = "${javaClass.name}_state_key"

    private val _state: MutableStateFlow<State> =
        MutableStateFlow(savedStateHandle?.get<State>(stateKey) ?: initialState)
    val state: StateFlow<State> = _state.asStateFlow()

    private val _oneTimeEvent: Channel<OneTimeEvent> = Channel(Channel.BUFFERED)
    val oneTimeEvent = _oneTimeEvent.receiveAsFlow()

    open fun handleAction(action: Action) {}

    protected fun setState(reducer: State.() -> State) {
        val newState = state.value.reducer()
        savedStateHandle?.set(stateKey, newState)
        _state.value = newState
    }

    protected fun sendEvent(builder: () -> OneTimeEvent) {
        val event = builder()
        viewModelScope.launch { _oneTimeEvent.send(event) }
    }

}
