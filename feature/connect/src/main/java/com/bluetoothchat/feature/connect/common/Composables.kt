package com.bluetoothchat.feature.connect.common

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bluetoothchat.core.bluetooth.message.ConnectionState
import com.bluetoothchat.core.ui.R
import com.bluetoothchat.core.ui.components.EmptyScreenContentWithAction
import com.bluetoothchat.core.ui.components.UserImage
import com.bluetoothchat.core.ui.model.ViewBtDeviceWithUser
import com.bluetoothchat.core.ui.theme.LocalChatAppColorScheme
import com.bluetoothchat.core.ui.theme.ScreenContentHorizontalPadding
import com.bluetoothchat.core.ui.theme.mainItemDivider
import com.bluetoothchat.core.ui.theme.onBackgroundInfoText
import com.bluetoothchat.core.ui.theme.onScreenBackground75
import com.bluetoothchat.core.ui.util.noRippleClickable

@Composable
internal fun NoBluetoothPermission(actionListener: () -> Unit) {
    EmptyScreenContentWithAction(
        modifier = Modifier.fillMaxSize(),
        text = stringResource(id = R.string.connect_permission_needed),
        actionText = stringResource(id = R.string.provide),
        actionClickListener = actionListener,
    )
}

@Composable
internal fun BluetoothDisabled(actionListener: () -> Unit) {
    EmptyScreenContentWithAction(
        modifier = Modifier.fillMaxSize(),
        text = stringResource(id = R.string.connect_bluetooth_disabled),
        actionText = stringResource(id = R.string.enable),
        actionClickListener = actionListener,
    )
}

@Composable
internal fun Divider(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(color = LocalChatAppColorScheme.current.mainItemDivider),
    )
}

@Composable
internal fun ConnectingProgressOverlay(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .noRippleClickable { }
            .background(color = LocalChatAppColorScheme.current.screenBackground.copy(0.75f)),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        CircularProgressIndicator(
            color = LocalChatAppColorScheme.current.onScreenBackground,
        )

        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(id = R.string.chat_input_connecting_message),
            style = TextStyle(
                fontSize = 16.sp,
                fontFamily = FontFamily.SansSerif,
                color = LocalChatAppColorScheme.current.onScreenBackground75,
                fontWeight = FontWeight.Medium,
            )
        )
    }
}

@Composable
internal fun SectionTitleText(text: String, modifier: Modifier = Modifier) {
    Text(
        modifier = modifier.padding(horizontal = ScreenContentHorizontalPadding * 2, vertical = 4.dp),
        text = text,
        style = TextStyle(
            fontFamily = FontFamily.SansSerif,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = LocalChatAppColorScheme.current.onBackgroundInfoText,
        ),
    )
}


@Composable
internal fun EmptyListText(text: String, modifier: Modifier = Modifier) {
    Text(
        modifier = modifier.padding(horizontal = ScreenContentHorizontalPadding, vertical = 16.dp),
        text = text,
        style = TextStyle(
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp,
            color = LocalChatAppColorScheme.current.onBackgroundInfoText,
        ),
        textAlign = TextAlign.Center,
    )
}

@Composable
internal fun BtDevice(
    device: ViewBtDeviceWithUser,
    clickListener: () -> Unit,
    modifier: Modifier = Modifier,
    rightContent: @Composable RowScope.() -> Unit = {},
) {
    val user = device.user
    val device = device.device

    val colorScheme = LocalChatAppColorScheme.current
    val textBaseColor = remember(device.connectionState) {
        when (device.connectionState) {
            ConnectionState.DISCONNECTED -> colorScheme.onScreenBackground
            ConnectionState.CONNECTING -> colorScheme.onScreenBackground75
            ConnectionState.CONNECTED -> colorScheme.accent
        }
    }

    Row(
        modifier = modifier
            .clickable { clickListener() }
            .padding(horizontal = ScreenContentHorizontalPadding, vertical = 8.dp)
            .wrapContentHeight(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (user != null) {
            UserImage(
                modifier = Modifier.size(32.dp),
                user = user,
                userDeviceAddress = user.deviceAddress,
            )
        } else {
            Icon(
                modifier = Modifier
                    .padding(4.dp)
                    .size(24.dp),
                painter = painterResource(id = R.drawable.ic_bluetooth),
                tint = LocalChatAppColorScheme.current.onScreenBackground75,
                contentDescription = "Bluetooth",
            )
        }

        Spacer(modifier = Modifier.width(8.dp))
        Column(modifier = Modifier) {
            Text(
                text = (device.device.name ?: "") + (user?.let { " (${it.userName})" } ?: ""),
                textAlign = TextAlign.Start,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = TextStyle(
                    fontSize = 15.sp,
                    fontFamily = FontFamily.SansSerif,
                    color = textBaseColor,
                    fontWeight = FontWeight.Medium,
                )
            )

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = device.device.address,
                textAlign = TextAlign.Start,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = TextStyle(
                    fontSize = 13.sp,
                    fontFamily = FontFamily.SansSerif,
                    color = textBaseColor.copy(alpha = textBaseColor.alpha * 0.75f),
                    fontWeight = FontWeight.Normal,
                )
            )
        }

        rightContent()
    }
}
