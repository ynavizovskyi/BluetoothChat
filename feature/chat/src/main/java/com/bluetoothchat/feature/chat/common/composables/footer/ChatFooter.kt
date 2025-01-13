package com.bluetoothchat.feature.chat.common.composables.footer

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bluetoothchat.core.ui.components.ContentCrossFade
import com.bluetoothchat.core.ui.components.CustomTextField
import com.bluetoothchat.core.ui.components.CustomTextFieldColors
import com.bluetoothchat.core.ui.components.ExpandVerticallyAnimatedVisibility
import com.bluetoothchat.core.ui.components.button.CustomButton
import com.bluetoothchat.core.ui.model.ViewMessage
import com.bluetoothchat.core.ui.model.primaryContent
import com.bluetoothchat.core.ui.theme.LocalChatAppColorScheme
import com.bluetoothchat.core.ui.theme.mainItemDivider
import com.bluetoothchat.feature.chat.R
import com.bluetoothchat.feature.chat.common.composables.QuotedMessage
import com.bluetoothchat.core.ui.R as CoreUiR

val ChatInputFieldHeight = 48.dp
private val ChatFooterHorizontalPadding = 16.dp
private const val MaxMessageLength = 1000

@Composable
internal fun ChatFooter(
    state: ChatFooterState,
    quotedMessage: ViewMessage?,
    actionListener: (ChatFooterAction) -> Unit,
    focusRequester: FocusRequester,
    modifier: Modifier = Modifier,
) {
    ContentCrossFade(
        modifier = modifier
            .background(color = LocalChatAppColorScheme.current.chatInputBackground),
        targetState = state,
        contentKey = { it },
    ) { animatedState ->
        when (animatedState) {
            is ChatFooterState.InfoWithButton -> {
                ChatFooterInfoWithButton(
                    modifier = Modifier.fillMaxWidth(),
                    message = animatedState.infoText.asString(),
                    buttonEnabled = animatedState.buttonEnabled,
                    buttonText = animatedState.buttonText.asString(),
                    connectClickListener = { actionListener(ChatFooterAction.ButtonClicked(id = animatedState.buttonId)) },
                )
            }

            is ChatFooterState.Info -> {
                ChatFooterInfoText(
                    modifier = Modifier.fillMaxWidth(),
                    text = animatedState.message.asString(),
                )
            }

            is ChatFooterState.InputField -> {
                ChatInputField(
                    modifier = Modifier.fillMaxWidth(),
                    sendButtonListener = { text -> actionListener(ChatFooterAction.SendMessageClicked(text)) },
                    fileButtonListener = { actionListener(ChatFooterAction.FileClicked) },
                    clearReplyButtonListener = { actionListener(ChatFooterAction.ClearReplyClicked) },
                    focusRequester = focusRequester,
                    quotedMessage = quotedMessage,
                )
            }
        }
    }
}

@Composable
internal fun ChatFooterInfoText(text: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .height(ChatInputFieldHeight)
            .padding(horizontal = ChatFooterHorizontalPadding),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = TextStyle(
                color = LocalChatAppColorScheme.current.onScreenBackground.copy(alpha = 0.5f),
                fontFamily = FontFamily.SansSerif,
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                fontStyle = FontStyle.Normal,
                textAlign = TextAlign.Center,
            ),
        )
    }
}

@Composable
internal fun ChatFooterInfoWithButton(
    message: String,
    buttonEnabled: Boolean,
    buttonText: String,
    connectClickListener: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .background(color = LocalChatAppColorScheme.current.chatInputBackground)
            .padding(horizontal = ChatFooterHorizontalPadding)
            .height(ChatInputFieldHeight),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = message,
            style = TextStyle(
                color = LocalChatAppColorScheme.current.onScreenBackground.copy(alpha = 0.5f),
                fontFamily = FontFamily.SansSerif,
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                fontStyle = FontStyle.Normal,
            ),
            modifier = Modifier.weight(1f)
        )

        Spacer(modifier = Modifier.width(16.dp))
        CustomButton(text = buttonText, onClick = connectClickListener, enabled = buttonEnabled)
    }
}

@Composable
internal fun ChatInputField(
    sendButtonListener: (String) -> Unit,
    fileButtonListener: () -> Unit,
    clearReplyButtonListener: () -> Unit,
    focusRequester: FocusRequester,
    modifier: Modifier = Modifier,
    quotedMessage: ViewMessage?,
) {
    Column(
        modifier = modifier
            .background(color = LocalChatAppColorScheme.current.chatInputBackground),
    ) {
        //This is needed to perform shrink animation after states' quoted message is set to null
        //We need a cached value to render animated shrink state
        var cachedQuotedMessage by remember { mutableStateOf<ViewMessage.Plain?>(null) }
        LaunchedEffect((quotedMessage as? ViewMessage.Plain).hashCode()) {
            if (quotedMessage is ViewMessage.Plain) cachedQuotedMessage = quotedMessage
        }
        ExpandVerticallyAnimatedVisibility(
            modifier = Modifier.fillMaxWidth(),
            visible = quotedMessage is ViewMessage.Plain,
        ) {
            cachedQuotedMessage?.let {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                            .fillMaxWidth()
                    ) {
                        QuotedMessage(
                            modifier = Modifier
                                .fillMaxWidth(),
                            userDeviceAddress = it.userDeviceAddress,
                            primaryContent = it.primaryContent(),
                            user = it.user,
                            //Leaving enough space for the close button
                            innerPaddingEnd = 40.dp,
                        )

                        Image(
                            modifier = Modifier
                                .padding(4.dp)
                                .clip(CircleShape)
                                .size(32.dp)
                                .clickable { clearReplyButtonListener() }
                                .padding(8.dp)
                                .align(Alignment.CenterEnd),
                            painter = rememberVectorPainter(image = Icons.Default.Clear),
                            colorFilter = ColorFilter.tint(
                                color = LocalChatAppColorScheme.current.onScreenBackground.copy(
                                    alpha = 0.5f
                                )
                            ),
                            contentDescription = "Remove quote",
                        )
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(color = LocalChatAppColorScheme.current.mainItemDivider),
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            var text by remember { mutableStateOf("") }
            CustomTextField(
                modifier = Modifier
                    .weight(1f)
                    .align(Alignment.CenterVertically)
                    .focusRequester(focusRequester),
                value = text,
                onValueChange = { newText: String ->
                    if (newText.length <= MaxMessageLength) {
                        text = newText
                    }
                },
                textStyle = TextStyle(
                    color = LocalChatAppColorScheme.current.onScreenBackground,
                    fontFamily = FontFamily.SansSerif,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Normal,
                    fontStyle = FontStyle.Normal,
                ),
                placeholder = {
                    Text(
                        text = stringResource(CoreUiR.string.chat_input_hint),
                        style = TextStyle(
                            color = LocalChatAppColorScheme.current.onScreenBackground.copy(alpha = 0.5f),
                            fontFamily = FontFamily.SansSerif,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Normal,
                            fontStyle = FontStyle.Normal,
                        ),
                        maxLines = 1,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                maxLines = 5,
                colors = CustomTextFieldColors(
                    cursorColor = LocalChatAppColorScheme.current.accent,
                )
            )

            Spacer(modifier = Modifier.width(16.dp))
            if (text.isNotEmpty()) {
                InputFieldActionImage(
                    modifier = Modifier.align(Alignment.Bottom),
                    painter = painterResource(id = R.drawable.ic_send),
                    color = LocalChatAppColorScheme.current.accent,
                    clickListener = {
                        sendButtonListener(text)
                        text = ""
                    },
                )
            } else {
                InputFieldActionImage(
                    modifier = Modifier.align(Alignment.Bottom),
                    painter = painterResource(id = CoreUiR.drawable.ic_image),
                    color = LocalChatAppColorScheme.current.onScreenBackground.copy(alpha = 0.5f),
                    clickListener = { fileButtonListener() },
                )
            }
        }
    }
}

@Composable
private fun InputFieldActionImage(
    painter: Painter,
    color: Color,
    clickListener: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Image(
        modifier = modifier
            .padding(4.dp)
            .clip(CircleShape)
            .size(40.dp)
            .clickable { clickListener() }
            .padding(6.dp),
        painter = painter,
        colorFilter = ColorFilter.tint(color = color),
        contentDescription = "Send",
    )
}
