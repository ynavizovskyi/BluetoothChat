package com.bluetoothchat.core.ui.components.dropdown

import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ripple.LocalRippleTheme
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.PopupProperties
import com.bluetoothchat.core.ui.theme.LocalChatAppColorScheme
import com.bluetoothchat.core.ui.util.crop
import kotlinx.coroutines.delay

@Composable
fun <I, A> ItemDropdownMenuContainer(
    item: I,
    actions: List<DropdownMenuItemModel<A>>,
    displayStrategy: DropDownDisplayStrategy<I, A>,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    val density = LocalDensity.current
    var isContextMenuVisible by remember { mutableStateOf(false) }
    var itemHeight by remember { mutableStateOf(0.dp) }
    var pressOffset by remember { mutableStateOf(DpOffset.Zero) }

    var tapped by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }

    val visibilityHandler = displayStrategy.visibilityHandler
    val screenHasVisiblePopup = visibilityHandler.screenHasVisiblePopup
    LaunchedEffect(isContextMenuVisible) {
        delay(100)
        screenHasVisiblePopup.value = isContextMenuVisible
    }

    LaunchedEffect(screenHasVisiblePopup.value) {
        if (!screenHasVisiblePopup.value && isContextMenuVisible) {
            isContextMenuVisible = screenHasVisiblePopup.value
        }
    }

    Box(
        modifier = modifier
            .then(
                if (displayStrategy.displayRipple) Modifier.indication(interactionSource, LocalIndication.current)
                else Modifier
            )
            .pointerInput(true) {
                detectTapGestures(
                    onTap = {
                        when (displayStrategy) {
                            is DropDownDisplayStrategy.OnPress -> {
                                if (!visibilityHandler.hasVisiblePopup) {
                                    isContextMenuVisible = actions.isNotEmpty()
                                    pressOffset = DpOffset(it.x.toDp(), it.y.toDp())
                                    displayStrategy.onActionsShown()
                                }
                            }

                            is DropDownDisplayStrategy.OnLongPress -> {
                                displayStrategy.onPressListener(item)
                            }
                        }
                    },
                    onLongPress = {
                        if (displayStrategy is DropDownDisplayStrategy.OnLongPress) {
                            if (!visibilityHandler.hasVisiblePopup) {
                                isContextMenuVisible = actions.isNotEmpty()
                                pressOffset = DpOffset(it.x.toDp(), it.y.toDp())
                                displayStrategy.onActionsShown()
                            }
                        }
                    },
                    onPress = {
                        if (displayStrategy.displayRipple) {
                            tapped = true

                            val press = PressInteraction.Press(it)
                            interactionSource.emit(press)
                            tryAwaitRelease()
                            interactionSource.emit(PressInteraction.Release(press))

                            tapped = false
                        }
                    },
                )
            }
            .onSizeChanged {
                itemHeight = with(density) { it.height.toDp() }
            },
    ) {
        content()


        DropdownMenu(
            isFocusable = visibilityHandler.isFocusable,
            expanded = isContextMenuVisible && visibilityHandler.hasVisiblePopup,
            offset = pressOffset.copy(y = pressOffset.y - itemHeight),
            dismissRequest = { isContextMenuVisible = false },
            items = actions,
            onItemClick = { action ->
                isContextMenuVisible = false
                displayStrategy.onActionClick(item, action)
            },
        )

    }
}

@Composable
fun <T> DropdownMenu(
    expanded: Boolean,
    offset: DpOffset,
    dismissRequest: () -> Unit,
    onItemClick: (T) -> Unit,
    items: List<DropdownMenuItemModel<T>>,
    isFocusable: Boolean = true,
) {
    //Stupid hack to preserve ripple color after setting material theme to change dropdown shape *facepalm*
    val rippleTheme = LocalIndication.current
    MaterialTheme(
        //This is the way to round corners in the dropdown menu ffs
        shapes = MaterialTheme.shapes.copy(extraSmall = RoundedCornerShape(4.dp)),
        colorScheme = MaterialTheme.colorScheme.copy(surface = Color.Transparent)
    ) {
        CompositionLocalProvider(LocalIndication provides rippleTheme) {
            DropdownMenu(
                modifier = Modifier
                    .background(color = LocalChatAppColorScheme.current.chatInputBackground)
                    //*double facepalm* a way to cut out hardcoded vertical paddings
                    .crop(vertical = 8.dp),
                expanded = expanded,
                onDismissRequest = dismissRequest,
                offset = offset,
                properties = PopupProperties(
                    focusable = isFocusable,
                    dismissOnClickOutside = true,
                    dismissOnBackPress = true,
                ),
            ) {
                items.forEach { menuItem ->
                    DropdownMenuItem(onClick = {
                        onItemClick(menuItem.data)
                    },
                        text = {
                            Text(
                                text = menuItem.text.asString(),
                                style = TextStyle(
                                    color = LocalChatAppColorScheme.current.onScreenBackground,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Normal,
                                    fontFamily = FontFamily.SansSerif,
                                ),
                            )
                        }
                    )
                }
            }
        }
    }
}
