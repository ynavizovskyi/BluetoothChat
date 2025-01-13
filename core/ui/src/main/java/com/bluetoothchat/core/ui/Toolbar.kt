package com.bluetoothchat.core.ui

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bluetoothchat.core.ui.components.UserImage
import com.bluetoothchat.core.ui.components.dropdown.DropdownMenu
import com.bluetoothchat.core.ui.components.dropdown.DropdownMenuItemModel
import com.bluetoothchat.core.ui.model.ViewUser
import com.bluetoothchat.core.ui.theme.LocalChatAppColorScheme

val ToolbarHeight = 56.dp

//val ToolbarHorizontalPadding = 4.dp
val ToolbarVisibleHorizontalPadding = 16.dp
val ToolbarActionSize = 40.dp

val ToolbarIconSize = 24.dp
val ToolbarIconInnerPadding = (ToolbarActionSize - ToolbarIconSize) / 2
val ToolbarIconHorizontalPadding = ToolbarVisibleHorizontalPadding - ToolbarIconInnerPadding

val ToolbarImageSize = 32.dp
val ToolbarImageInnerPadding = (ToolbarActionSize - ToolbarImageSize) / 2
val ToolbarImageHorizontalPadding = ToolbarVisibleHorizontalPadding - ToolbarImageInnerPadding

//val ToolbarTitleNoActionHorizontalPadding = ToolbarVisibleHorizontalPadding - ToolbarHorizontalPadding

@Composable
fun SimpleChatAppToolbar(
    title: String,
    modifier: Modifier = Modifier,
    backgroundColor: Color = LocalChatAppColorScheme.current.toolbar,
    navigationListener: (() -> Unit)? = null,
) {
    ChatAppToolbar(modifier = modifier, backgroundColor = backgroundColor) {
        if (navigationListener == null) {
            Spacer(modifier = Modifier.width(ToolbarVisibleHorizontalPadding))
        } else {
            Spacer(modifier = Modifier.width(ToolbarIconHorizontalPadding))
            ToolbarBackIconButton(navigateUp = navigationListener)
            Spacer(modifier = Modifier.width(ToolbarIconHorizontalPadding))
        }

        ToolbarTitleText(text = title)
    }
}

@Composable
fun ChatAppToolbar(
    modifier: Modifier = Modifier,
    backgroundColor: Color = LocalChatAppColorScheme.current.toolbar,
    content: @Composable RowScope.() -> Unit = {},
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(ToolbarHeight)
            .background(color = backgroundColor),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        content()
    }
}

@Composable
fun ToolbarTitleText(text: String, modifier: Modifier = Modifier) {
    Text(
        modifier = modifier,
        text = text,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        style = TextStyle(
            color = LocalChatAppColorScheme.current.onToolbar,
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.Medium,
            fontSize = 20.sp,
            platformStyle = PlatformTextStyle(includeFontPadding = false),
        ),
    )
}

@Composable
fun ToolbarSubtitleText(text: String, modifier: Modifier = Modifier) {
    Text(
        modifier = modifier,
        text = text,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        style = TextStyle(
            color = LocalChatAppColorScheme.current.onToolbar.copy(alpha = 0.5f),
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp,
            platformStyle = PlatformTextStyle(includeFontPadding = false),
        ),
    )
}

@Composable
fun ToolbarUserImage(user: ViewUser?, modifier: Modifier = Modifier, clickListener: (() -> Unit)? = null) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .size(ToolbarActionSize)
            .then(clickListener?.let { Modifier.clickable { it() } } ?: Modifier),
        contentAlignment = Alignment.Center,
    ) {
        UserImage(
            modifier = Modifier.size(ToolbarImageSize),
            user = user,
            userDeviceAddress = "",
        )
    }
}

@Composable
fun ToolbarIconButton(@DrawableRes iconResId: Int, modifier: Modifier = Modifier, clickListener: (() -> Unit)? = null) {
    ToolbarIconButton(
        painter = painterResource(id = iconResId),
        modifier = modifier,
        clickListener = clickListener,
    )
}

@Composable
fun ToolbarIconButton(vector: ImageVector, modifier: Modifier = Modifier, clickListener: (() -> Unit)? = null) {
    ToolbarIconButton(
        painter = rememberVectorPainter(image = vector),
        modifier = modifier,
        clickListener = clickListener,
    )
}

@Composable
fun ToolbarIconButton(painter: Painter, modifier: Modifier = Modifier, clickListener: (() -> Unit)? = null) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .size(ToolbarActionSize)
            .then(clickListener?.let { Modifier.clickable { it() } } ?: Modifier),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painter,
            tint = LocalChatAppColorScheme.current.onToolbar,
            contentDescription = "Toolbar action",
        )
    }
}

@Composable
fun ToolbarBackIconButton(modifier: Modifier = Modifier, navigateUp: () -> Unit) {
    IconButton(
        modifier = modifier,
        onClick = navigateUp,
        colors = IconButtonDefaults.iconButtonColors(contentColor = LocalChatAppColorScheme.current.onToolbar)
    ) {
        Icon(
            imageVector = Icons.Default.ArrowBack,
            contentDescription = "Navigate up",
        )
    }
}

@Composable
fun <T> ToolbarDropdownIconButton(
    actions: List<DropdownMenuItemModel<T>>,
    buttonClickListener: () -> Unit,
    actionClickListener: (T) -> Unit,
) {
    val configuration = LocalConfiguration.current
    val screenWidth = remember { configuration.screenWidthDp.dp }
    var isContextMenuVisible by remember { mutableStateOf(false) }
    //Rightmost position
    var pressOffset by remember { mutableStateOf(DpOffset(screenWidth, 4.dp)) }

    ToolbarIconButton(
        vector = Icons.Default.MoreVert,
        clickListener = {
            isContextMenuVisible = !isContextMenuVisible
            buttonClickListener()
        },
    )
    Spacer(modifier = Modifier.width(ToolbarIconHorizontalPadding))

    DropdownMenu(
        expanded = isContextMenuVisible,
        offset = pressOffset,
        dismissRequest = { isContextMenuVisible = false },
        items = actions,
        onItemClick = { action ->
            isContextMenuVisible = false
            actionClickListener(action)
        },
    )
}
