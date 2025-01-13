package com.bluetoothchat.core.ui.components.dropdown

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf

sealed interface DropDownDisplayStrategy<I, A> {
    val displayRipple: Boolean
    val onActionsShown: () -> Unit
    val onActionClick: (I, A) -> Unit
    val visibilityHandler: DropDownVisibilityHandler

    data class OnPress<I, A>(
        override val displayRipple: Boolean = true,
        override val onActionsShown: () -> Unit = {},
        override val onActionClick: (I, A) -> Unit,
        override val visibilityHandler: DropDownVisibilityHandler = DropDownVisibilityHandler.Default,
    ) : DropDownDisplayStrategy<I, A>

    data class OnLongPress<I, A>(
        override val displayRipple: Boolean = true,
        override val onActionsShown: () -> Unit = {},
        override val onActionClick: (I, A) -> Unit,
        override val visibilityHandler: DropDownVisibilityHandler = DropDownVisibilityHandler.Default,
        //When simple click is performed
        val onPressListener: (I) -> Unit = {},
    ) : DropDownDisplayStrategy<I, A>

}

//This is an extremely hacky way to fix popup focus issues
//By default android popup needs to be focusable in order to handle back and outside touch events properly
//But when it is focusable when shown it hides keyboard (which is very annoying)
//This allows to achieve the behaviour similar to the focused popup without hiding the keyboard
sealed interface DropDownVisibilityHandler {
    val screenHasVisiblePopup: MutableState<Boolean>
    val isFocusable: Boolean
    val hasVisiblePopup: Boolean

    data object Default : DropDownVisibilityHandler {
        override val screenHasVisiblePopup: MutableState<Boolean> = mutableStateOf(false)
        override val isFocusable: Boolean = true
        override val hasVisiblePopup: Boolean
            get() = screenHasVisiblePopup.value
    }

    data class Custom(override val screenHasVisiblePopup: MutableState<Boolean>) : DropDownVisibilityHandler {
        override val isFocusable: Boolean = false
        override val hasVisiblePopup: Boolean
            get() = screenHasVisiblePopup.value
    }

}
