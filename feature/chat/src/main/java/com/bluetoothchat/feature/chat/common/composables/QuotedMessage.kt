package com.bluetoothchat.feature.chat.common.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.bluetoothchat.core.filemanager.file.FileState
import com.bluetoothchat.core.ui.model.ViewMessageContent
import com.bluetoothchat.core.ui.model.ViewQuotedMessage
import com.bluetoothchat.core.ui.model.ViewUser
import com.bluetoothchat.core.ui.model.toColor
import com.bluetoothchat.core.ui.model.toMessageAuthorName
import com.bluetoothchat.core.ui.model.toShortDescription
import com.bluetoothchat.core.ui.theme.LocalChatAppColorScheme
import java.io.File

@Composable
internal fun QuotedMessage(message: ViewQuotedMessage, modifier: Modifier = Modifier) {
    QuotedMessage(
        userDeviceAddress = message.userDeviceAddress,
        primaryContent = message.primaryContent,
        user = message.user,
        modifier = modifier,
    )
}

@Composable
internal fun QuotedMessage(
    userDeviceAddress: String,
    primaryContent: ViewMessageContent?,
    user: ViewUser?,
    modifier: Modifier = Modifier,
    innerPaddingEnd: Dp = 8.dp,
) {
    val contentText = primaryContent?.toShortDescription() ?: ""
    val username = user.toMessageAuthorName(deviceAddress = userDeviceAddress)
    val userColor = user.toColor()

    Row(
        modifier = modifier
            .height(IntrinsicSize.Min)
            .clip(RoundedCornerShape(4.dp))
            .background(color = userColor.copy(alpha = 0.10f))
    ) {
        Box(
            modifier = Modifier
                .width(2.dp)
                .fillMaxHeight()
                .background(color = userColor)
        )
        Spacer(modifier = Modifier.width(8.dp))

        (primaryContent as? ViewMessageContent.Image)?.let { content ->
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .size(32.dp)
                    .background(Color(content.dominantColor))
                    .align(Alignment.CenterVertically),
            ) {
                when (val file = content.file) {
                    is FileState.Downloaded -> {
                        val coilFile = remember(file.path) { File(file.path) }
                        val imageRequest = ImageRequest.Builder(LocalContext.current)
                            .data(coilFile)
                            .crossfade(true)
                            .build()

                        AsyncImage(
                            modifier = Modifier.fillMaxSize(),
                            model = imageRequest,
                            contentScale = ContentScale.Crop,
                            contentDescription = "Chat image",
                        )
                    }

                    else -> Unit
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        Column {
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                modifier = Modifier.padding(end = innerPaddingEnd),
                text = username,
                style = TextStyle(
                    color = userColor.copy(alpha = 0.75f),
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = FontFamily.SansSerif,
                    fontSize = 12.sp,
                )
            )

            Text(
                modifier = Modifier.padding(end = innerPaddingEnd),
                text = contentText,
                textAlign = TextAlign.Start,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = TextStyle(
                    fontSize = 14.sp,
                    fontFamily = FontFamily.SansSerif,
                    //TODO:
                    color = LocalChatAppColorScheme.current.othersMessageContent.copy(alpha = 0.75f),
                    fontWeight = FontWeight.Normal,
                )
            )
            Spacer(modifier = Modifier.height(2.dp))
        }

//        Spacer(modifier = Modifier.width(innerPaddingEnd))
    }

    Spacer(modifier = Modifier.height(2.dp))

}
