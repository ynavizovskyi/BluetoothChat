package com.bluetoothchat.feature.chat.common.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.bluetoothchat.core.filemanager.file.FileState
import com.bluetoothchat.core.ui.components.UserImage
import com.bluetoothchat.core.ui.components.dropdown.DropDownDisplayStrategy
import com.bluetoothchat.core.ui.components.dropdown.DropdownMenuItemModel
import com.bluetoothchat.core.ui.components.dropdown.ItemDropdownMenuContainer
import com.bluetoothchat.core.ui.model.UiText
import com.bluetoothchat.core.ui.model.ViewMessage
import com.bluetoothchat.core.ui.model.ViewMessageAction
import com.bluetoothchat.core.ui.model.ViewMessageContent
import com.bluetoothchat.core.ui.model.ViewUser
import com.bluetoothchat.core.ui.model.primaryContent
import com.bluetoothchat.core.ui.model.text
import com.bluetoothchat.core.ui.model.toColor
import com.bluetoothchat.core.ui.model.toMessageAuthorName
import com.bluetoothchat.core.ui.theme.Colors
import com.bluetoothchat.core.ui.theme.LocalChatAppColorScheme
import com.bluetoothchat.feature.chat.common.model.ViewChatItem
import com.bluetoothchat.feature.chat.common.toBackgroundColor
import com.bluetoothchat.feature.chat.common.toContentColor
import java.io.File

private val ChatMessageRadius = 8.dp
private val MaxImageWidth = 280.dp
private val MaxImageHeight = 360.dp
private val MaxImageSizeAspectRatio = MaxImageWidth / MaxImageHeight

val TimeTextStyle = TextStyle(
    fontFamily = FontFamily.SansSerif,
    fontSize = 12.sp,
)

@Composable
internal fun ChatItem(
    item: ViewChatItem,
    showOtherUserImages: Boolean,
    userClickListener: (ViewUser) -> Unit,
    imageClickListener: (ViewMessage) -> Unit,
    dropDownDisplayStrategy: DropDownDisplayStrategy<ViewChatItem, ViewMessageAction>,
    modifier: Modifier = Modifier,
) {
    when (item) {
        is ViewChatItem.DateHeader -> ChatDateHeader(modifier = modifier, date = item.date)

        is ViewChatItem.Message -> {
            val message = item.message
            val actions = remember(message) {
                when (message) {
                    is ViewMessage.GroupUpdate -> emptyList()
                    is ViewMessage.Plain -> message.actions.map {
                        DropdownMenuItemModel(text = UiText.Resource(it.nameStringRes), data = it)
                    }
                }
            }

            ItemDropdownMenuContainer(
                actions = actions,
                item = item,
                displayStrategy = dropDownDisplayStrategy,
                modifier = modifier
                    .padding(
                        start = 8.dp,
                        top = if (item.extendedTopPadding && message is ViewMessage.Plain) 4.dp else 2.dp,
                        end = 8.dp,
                        bottom = 2.dp,
                    )
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .focusable(false)
            ) {
                when (message) {
                    is ViewMessage.Plain -> {
                        val alignment = if (message.isMine) Alignment.CenterEnd else Alignment.CenterStart

                        Row(
                            modifier = Modifier.align(alignment),
                        ) {
                            if (showOtherUserImages) {
                                Box(modifier = Modifier.width(40.dp)) {
                                    if (message.displayUserImage) {
                                        UserImage(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .aspectRatio(1f),
                                            user = message.user,
                                            userDeviceAddress = message.userDeviceAddress,
                                            clickListener = { message.user?.let { userClickListener(it) } },
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                            }

                            when (val content = message.primaryContent()) {
                                is ViewMessageContent.Text -> {
                                    ChatMessageText(
                                        message = message,
                                        content = content,
                                    )
                                }

                                is ViewMessageContent.Image -> {
                                    ChatMessageImage(
                                        content = content,
                                        message = message,
                                        imageClickListener = imageClickListener,
                                    )
                                }
                            }
                        }
                    }

                    is ViewMessage.GroupUpdate -> {
                        ChatMessageGroupUpdate(message = message)
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatDateHeader(date: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .fillMaxWidth()
            .wrapContentHeight(),
    ) {
        Text(
            modifier = Modifier
                .align(Alignment.Center)
                .background(
                    color = LocalChatAppColorScheme.current.othersMessageBackground.copy(alpha = 0.75f),
                    shape = RoundedCornerShape(50)
                )
                .padding(horizontal = 8.dp, vertical = 4.dp),
            text = date,
            style = TextStyle(
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = FontFamily.SansSerif,
                color = LocalChatAppColorScheme.current.othersMessageContent.copy(alpha = 0.75f),
            ),
        )
    }
}

@Composable
private fun ChatMessageGroupUpdate(
    message: ViewMessage.GroupUpdate,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .padding(horizontal = 16.dp, vertical = 2.dp)
            .fillMaxWidth()
            .wrapContentHeight()
    ) {
        Text(
            modifier = Modifier
                .align(Alignment.Center)
                .background(
                    color = LocalChatAppColorScheme.current.othersMessageBackground.copy(alpha = 0.75f),
                    shape = RoundedCornerShape(50)
                )
                .padding(horizontal = 8.dp, vertical = 4.dp),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            text = message.text(),
            style = TextStyle(
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = FontFamily.SansSerif,
                color = LocalChatAppColorScheme.current.othersMessageContent.copy(alpha = 0.75f),
            ),
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ChatMessageText(
    message: ViewMessage.Plain,
    content: ViewMessageContent.Text,
    modifier: Modifier = Modifier,
) {
    val contentColor = message.toContentColor()

    Column(
        modifier = modifier
            .widthIn(max = 300.dp)
            .width(IntrinsicSize.Max)
            .clip(RoundedCornerShape(ChatMessageRadius))
            .background(message.toBackgroundColor())
            .padding(horizontal = 8.dp, vertical = 4.dp),
    ) {
        if (message.displayUserName) {
            UsernameText(message = message)
        }

        message.quotedMessage?.let {
            QuotedMessage(
                //No additional horizontal padding needed for text
                modifier = Modifier
                    .padding(horizontal = 0.dp, vertical = 2.dp)
                    .fillMaxWidth(),
                message = it,
            )
        }

        FlowRow(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                modifier = Modifier
                    .padding(bottom = 2.dp),
                text = content.text,
                style = TextStyle(
                    fontFamily = FontFamily.SansSerif,
                    fontSize = 16.sp,
                    color = contentColor,
                )
            )

            Text(
                modifier = Modifier
                    .padding(start = 8.dp)
                    .align(Alignment.Bottom)
                    .weight(1f),
                text = message.formattedTime,
                textAlign = TextAlign.End,
                style = TimeTextStyle.copy(color = contentColor.copy(alpha = 0.5f))
            )
        }
    }
}

@Composable
private fun ChatMessageImage(
    content: ViewMessageContent.Image,
    message: ViewMessage.Plain,
    imageClickListener: (ViewMessage) -> Unit,
    modifier: Modifier = Modifier,
) {
    val imagePadding = 4.dp
    Column(
        modifier = modifier
            .wrapContentHeight()
            .wrapContentWidth()
            .width(IntrinsicSize.Max)
            .clip(RoundedCornerShape(ChatMessageRadius))
            .background(message.toBackgroundColor())
            .padding(imagePadding),
    ) {
        if (message.displayUserName) {
            UsernameText(message = message)
        }

        message.quotedMessage?.let {
            QuotedMessage(
                modifier = Modifier
                    .padding(horizontal = 4.dp, vertical = 2.dp)
                    .fillMaxWidth(),
                message = it,
            )
        }

        val sizeModifier = remember(content.aspectRatio) {
            val matchWidthFirst = content.aspectRatio > MaxImageSizeAspectRatio
            if (matchWidthFirst) {
                Modifier
                    .width(MaxImageWidth)
                    .aspectRatio(content.aspectRatio)
            } else {
                Modifier
                    .height(MaxImageHeight)
                    .aspectRatio(content.aspectRatio)
            }
        }

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(ChatMessageRadius - imagePadding))
                .then(sizeModifier)
                .aspectRatio(content.aspectRatio)
                .background(Color(content.dominantColor))
        ) {
            when (val file = content.file) {
                is FileState.Downloaded -> {
                    val coilFile = remember(file.path) { File(file.path) }
                    val imageRequest = ImageRequest.Builder(LocalContext.current)
                        .data(coilFile)
                        .crossfade(true)
                        .build()

                    AsyncImage(
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable { imageClickListener(message) },
                        model = imageRequest,
                        contentScale = ContentScale.FillWidth,
                        contentDescription = "Chat image",
                    )
                }

                is FileState.Downloading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize(),
                    ) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.5f))
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(4.dp),
                                color = Colors.White,
                                trackColor = Colors.White.copy(alpha = 0.2f),
                                progress = (file.bytesDownloaded.toFloat() / file.fileSizeBytes.toFloat()),
                                strokeWidth = 3.dp,
                            )
                        }
                    }
                }

                else -> {

                }
            }

            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(4.dp)
                    .wrapContentWidth()
                    .wrapContentHeight()
                    .background(
                        color = Color.Black.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(50),
                    )
            ) {
                Text(
                    modifier = Modifier
                        .padding(horizontal = 6.dp, vertical = 0.dp)
                        .align(Alignment.BottomEnd),
                    text = message.formattedTime,
                    style = TimeTextStyle.copy(color = Color.White.copy(alpha = 0.75f))
                )
            }
        }
    }
}

@Composable
private fun UsernameText(message: ViewMessage.Plain, modifier: Modifier = Modifier) {
    val username = message.user.toMessageAuthorName(deviceAddress = message.userDeviceAddress)

    Text(
        text = username,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        style = TextStyle(
            fontFamily = FontFamily.SansSerif,
            color = message.user.toColor(),
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
        )
    )
    Spacer(modifier = Modifier.height(2.dp))
}
