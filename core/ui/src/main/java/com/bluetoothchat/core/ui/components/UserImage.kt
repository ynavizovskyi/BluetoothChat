package com.bluetoothchat.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.bluetoothchat.core.filemanager.file.FileState
import com.bluetoothchat.core.ui.model.ViewChat
import com.bluetoothchat.core.ui.model.ViewUser
import com.bluetoothchat.core.ui.theme.Colors
import com.bluetoothchat.core.ui.theme.LocalChatAppColorScheme
import java.io.File

private val gradientBrush = Brush.verticalGradient(
    0f to Color.White.copy(alpha = 0.15f),
    0.5f to Color.Black.copy(alpha = 0f),
    1f to Color.Black.copy(alpha = 0.15f),
)

@Composable
fun UserImage(
    user: ViewUser?,
    userDeviceAddress: String,
    modifier: Modifier = Modifier,
    displayConnectedIndicator: Boolean = true,
    clickListener: (() -> Unit)? = null
) {
    val userColor = remember(user) {
        user?.let { Color(it.color) } ?: Colors.userColor2
    }

    BoxWithConstraints(modifier = modifier) {
        ChatImage(
            modifier = Modifier.fillMaxSize(),
            imageFileState = user?.pictureFileState,
            chatName = user?.userName ?: userDeviceAddress,
            backgroundColor = userColor,
            clickListener = clickListener,
        )

        if (displayConnectedIndicator && user?.isConnected == true) {
            val indicatorSize = maxWidth / 4
            val indicatorBorderWidth = indicatorSize / 12
            Box(
                modifier = Modifier
                    .size(indicatorSize)
                    .background(color = LocalChatAppColorScheme.current.screenBackground, shape = CircleShape)
                    .padding(indicatorBorderWidth)
                    .background(color = LocalChatAppColorScheme.current.accent, shape = CircleShape)
                    .align(Alignment.BottomEnd),
            )
        }
    }
}

@Composable
fun GroupChatImage(
    chat: ViewChat.Group?,
    modifier: Modifier = Modifier,
    clickListener: (() -> Unit)? = null,
) {
    ChatImage(
        modifier = modifier,
        imageFileState = chat?.pictureFileState,
        chatName = chat?.name,
        clickListener = clickListener,
    )
}

@Composable
fun ChatImage(
    imageFileState: FileState?,
    chatName: String?,
    modifier: Modifier = Modifier,
    backgroundColor: Color = Colors.userColor2,
    clickListener: (() -> Unit)? = null,
) {
    BoxWithConstraints(modifier = modifier
        .clip(CircleShape)
        .then(clickListener?.let { Modifier.clickable { it() } } ?: Modifier)
        .background(backgroundColor)
        .background(gradientBrush)
    ) {
        if (imageFileState is FileState.Downloaded) {
            val coilFile = remember(imageFileState.path) { File(imageFileState.path) }
            val imageRequest = ImageRequest.Builder(LocalContext.current)
                .data(coilFile)
                .crossfade(true)
                .build()
            AsyncImage(
                modifier = Modifier.fillMaxSize(),
                model = imageRequest,
                contentDescription = "User Image",
            )
        } else {
            NoResourceImage(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .align(Alignment.Center),
                name = chatName,
            )
        }
    }
}

@Composable
private fun BoxWithConstraintsScope.NoResourceImage(name: String?, modifier: Modifier = Modifier) {
    val textSize = maxHeight * 0.4f
    val textSizeSp = with(LocalDensity.current) { textSize.toSp() }

    val text = remember(name) {
        if (!name.isNullOrEmpty()) {
            val pieces = name.split(" ").filter { it.isNotEmpty() }
            if (pieces.size > 1) {
                pieces[0][0].toString().toUpperCase() + pieces[1][0].toString().toUpperCase()
            } else {
                name.getOrNull(0)?.toString()?.toUpperCase() ?: ""
            }
        } else {
            ""
        }
    }

    Text(
        modifier = modifier,
        text = text,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        textAlign = TextAlign.Center,
        style = TextStyle(
            color = LocalChatAppColorScheme.current.onToolbar,
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.Medium,
            fontSize = textSizeSp,
        ),
    )
}
