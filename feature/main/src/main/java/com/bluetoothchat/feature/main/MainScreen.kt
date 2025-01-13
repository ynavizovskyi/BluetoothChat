package com.bluetoothchat.feature.main

import android.app.Activity
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bluetoothchat.core.permission.BluetoothPermissionType
import com.bluetoothchat.core.permission.PermissionStatus
import com.bluetoothchat.core.permission.getBluetoothPermissions
import com.bluetoothchat.core.permission.isGranted
import com.bluetoothchat.core.permission.rememberBluetoothPermissionsState
import com.bluetoothchat.core.permission.rememberNotificationPermissionState
import com.bluetoothchat.core.ui.ChatAppToolbar
import com.bluetoothchat.core.ui.ToolbarIconButton
import com.bluetoothchat.core.ui.ToolbarIconHorizontalPadding
import com.bluetoothchat.core.ui.ToolbarImageHorizontalPadding
import com.bluetoothchat.core.ui.ToolbarTitleText
import com.bluetoothchat.core.ui.ToolbarUserImage
import com.bluetoothchat.core.ui.components.ChatAppActionSnackbar
import com.bluetoothchat.core.ui.components.ContentCrossFade
import com.bluetoothchat.core.ui.components.GroupChatImage
import com.bluetoothchat.core.ui.components.ScreenContainer
import com.bluetoothchat.core.ui.components.UserImage
import com.bluetoothchat.core.ui.components.dropdown.DropDownDisplayStrategy
import com.bluetoothchat.core.ui.components.dropdown.DropdownMenuItemModel
import com.bluetoothchat.core.ui.components.dropdown.ItemDropdownMenuContainer
import com.bluetoothchat.core.ui.model.UiText
import com.bluetoothchat.core.ui.model.ViewChat
import com.bluetoothchat.core.ui.model.ViewChatAction
import com.bluetoothchat.core.ui.model.ViewChatWithMessages
import com.bluetoothchat.core.ui.model.ViewMessage
import com.bluetoothchat.core.ui.model.primaryContent
import com.bluetoothchat.core.ui.model.text
import com.bluetoothchat.core.ui.model.toMessageAuthorName
import com.bluetoothchat.core.ui.model.toShortColor
import com.bluetoothchat.core.ui.model.toShortDescription
import com.bluetoothchat.core.ui.navigation.StartScreenDestinationStyle
import com.bluetoothchat.core.ui.theme.LocalChatAppColorScheme
import com.bluetoothchat.core.ui.theme.mainItemDivider
import com.bluetoothchat.core.ui.theme.mainItemMessageText
import com.bluetoothchat.core.ui.theme.mainItemTimeText
import com.bluetoothchat.core.ui.util.launchInAppReview
import com.bluetoothchat.core.ui.util.observeWithLifecycle
import com.bluetoothchat.core.ui.util.openAppSystemSettings
import com.bluetoothchat.core.ui.util.showGrantPermissionSnackbar
import com.bluetoothchat.feature.main.contract.MainAction
import com.bluetoothchat.feature.main.contract.MainEvent
import com.bluetoothchat.feature.main.contract.MainState
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.ramcosta.composedestinations.annotation.Destination
import com.bluetoothchat.core.ui.R as CoreUiR

@Destination(start = true, style = StartScreenDestinationStyle::class)
@Composable
fun MainScreen(navigator: MainNavigator) {
    val viewModel: MainViewModel = hiltViewModel()
    val viewState by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    ObserveOneTimeEvents(
        viewModel = viewModel,
        navigator = navigator,
        snackbarHostState = snackbarHostState,
    )
    MainScreen(
        state = viewState,
        snackbarHostState = snackbarHostState,
        actionListener = { viewModel.handleAction(it) },
    )
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun ObserveOneTimeEvents(
    viewModel: MainViewModel,
    navigator: MainNavigator,
    snackbarHostState: SnackbarHostState,
) {
    val notificationPermissionState = rememberNotificationPermissionState(
        onStatusChanged = {
            viewModel.handleAction(MainAction.OnNotificationPermissionStatusChanged(status = it))
        }
    )
    LaunchedEffect(Unit) {
        if (notificationPermissionState != null) {
            val permissionStatus = if (notificationPermissionState.isGranted()) {
                PermissionStatus.Granted(isInitial = true)
            } else {
                PermissionStatus.Denied(isInitial = true, shouldShowRationale = false)
            }
            viewModel.handleAction(MainAction.OnNotificationPermissionStatusChanged(status = permissionStatus))
        } else {
            val status = PermissionStatus.Granted(isInitial = true)
            viewModel.handleAction(MainAction.OnNotificationPermissionStatusChanged(status = status))
        }
    }

    val bluetoothPermissions = remember { getBluetoothPermissions() }
    val bluetoothPermissionsState = rememberBluetoothPermissionsState(
        bluetoothPermissions = bluetoothPermissions,
        onStatusChanged = { permissionStatus ->
            viewModel.handleAction(
                MainAction.OnBluetoothPermissionsStatusChanged(
                    status = permissionStatus,
                    permissions = bluetoothPermissions
                )
            )
        },
    )
    LaunchedEffect(Unit) {
        val permissionStatus = if (bluetoothPermissionsState.isGranted()) {
            PermissionStatus.Granted(isInitial = true)
        } else {
            PermissionStatus.Denied(isInitial = true, shouldShowRationale = false)
        }
        viewModel.handleAction(
            MainAction.OnBluetoothPermissionsStatusChanged(
                status = permissionStatus,
                permissions = bluetoothPermissions,
            )
        )
    }

    val activity = LocalContext.current as Activity
    val coroutineScope = rememberCoroutineScope()
    viewModel.oneTimeEvent.observeWithLifecycle(minActiveState = Lifecycle.State.RESUMED) { event ->
        when (event) {
            is MainEvent.ShowDialog -> navigator.showDialog(event.params)
            is MainEvent.RequestNotificationPermission -> notificationPermissionState?.launchPermissionRequest()
            is MainEvent.RequestBluetoothPermissions -> bluetoothPermissionsState.launchMultiplePermissionRequest()
            is MainEvent.ShowGrantBluetoothPermissionsSnackbar -> {
                val messageResId = when (event.permssionType) {
                    BluetoothPermissionType.LOCATION -> CoreUiR.string.permission_denied_message_location
                    BluetoothPermissionType.BLUETOOTH -> CoreUiR.string.permission_denied_message_bluetooth
                }
                snackbarHostState.showGrantPermissionSnackbar(
                    coroutineScope = coroutineScope,
                    context = activity,
                    messageResId = messageResId,
                    onActionClicked = { viewModel.handleAction(MainAction.OpenSystemAppSettingsClicked) },
                )
            }

            is MainEvent.ShowGrantNotificationPermissionsSnackbar -> Unit
            is MainEvent.NavigateToConnectScreen -> navigator.navigateToConnectScreen()
            is MainEvent.NavigateToSettings -> navigator.navigateToSettings()
            is MainEvent.OpenSystemAppSettings -> activity.openAppSystemSettings()
            is MainEvent.LaunchInAppReview -> activity.launchInAppReview(onComplete = {
                viewModel.handleAction(MainAction.OnLaunchInAppReviewResult(result = it))
            })

            is MainEvent.NavigateToPrivateChatScreen -> navigator.navigateToPrivateChatScreen(event.chatId)
            is MainEvent.NavigateToGroupChatScreen -> navigator.navigateToGroupChatScreen(event.chatId)
            is MainEvent.NavigateToCurrentUserProfileScreen -> navigator.navigateToCurrentUserProfileScreen()
        }
    }

    navigator.OnDialogResult {
        viewModel.handleAction(MainAction.OnDialogResult(it))
    }
}

@Composable
private fun MainScreen(
    state: MainState,
    snackbarHostState: SnackbarHostState,
    actionListener: (MainAction) -> Unit,
) {
    ScreenContainer(
        topAppBar = { MainToolbar(state = state, actionListener = actionListener) }
    ) {
        ContentCrossFade(
            modifier = Modifier.fillMaxSize(),
            targetState = state,
            contentKey = { it::class },
        ) { animatedState ->
            when (animatedState) {
                is MainState.Loading -> {
                    //Show some progress
                }

                is MainState.Loaded -> {
                    LoadedScreenContent(
                        state = animatedState,
                        snackbarHostState = snackbarHostState,
                        actionListener = actionListener,
                    )
                }
            }
        }
    }
}

@Composable
private fun MainToolbar(state: MainState, actionListener: (MainAction) -> Unit) {
    ChatAppToolbar {
        Spacer(modifier = Modifier.width(ToolbarImageHorizontalPadding))
        ToolbarUserImage(
            user = (state as? MainState.Loaded)?.user,
            clickListener = { actionListener(MainAction.ProfileClicked) },
        )

        Spacer(modifier = Modifier.width(ToolbarImageHorizontalPadding))
        ToolbarTitleText(modifier = Modifier.weight(1f), text = stringResource(id = CoreUiR.string.screen_title_main))

        ToolbarIconButton(
            iconResId = R.drawable.ic_settings,
            clickListener = { actionListener(MainAction.SettingsClicked) },
        )
        Spacer(modifier = Modifier.width(ToolbarIconHorizontalPadding))
    }
}

@Composable
private fun BoxScope.LoadedScreenContent(
    state: MainState.Loaded,
    snackbarHostState: SnackbarHostState,
    actionListener: (MainAction) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = LocalChatAppColorScheme.current.screenBackground),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
        ) {
            ContentCrossFade(
                modifier = Modifier.fillMaxSize(),
                targetState = state.chats,
                contentKey = { it.isEmpty() },
            ) { animatedChats ->
                if (animatedChats.isEmpty()) {
                    EmptyChatList(modifier = Modifier.fillMaxSize())
                } else {
                    ChatList(
                        modifier = Modifier.fillMaxSize(),
                        chats = animatedChats,
                        actionListener = actionListener,
                    )
                }
            }

            FloatingActionButton(
                modifier = Modifier
                    .padding(16.dp)
                    .align(Alignment.BottomEnd),
                containerColor = LocalChatAppColorScheme.current.accent,
                contentColor = LocalChatAppColorScheme.current.onAccent,
                onClick = { actionListener(MainAction.CreateNewButtonClicked) },
            ) {
                Icon(Icons.Filled.Add, "Floating action button.")
            }

            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier
                    .padding(16.dp)
                    .align(Alignment.BottomCenter),
            ) {
                ChatAppActionSnackbar(it)
            }
        }
    }
}

@Composable
private fun EmptyChatList(modifier: Modifier = Modifier) {
    Box(modifier = modifier) {
        Text(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .align(Alignment.Center),
            text = stringResource(id = CoreUiR.string.main_empty_message),
            textAlign = TextAlign.Center,
            style = TextStyle(
                fontSize = 16.sp,
                fontFamily = FontFamily.SansSerif,
                color = LocalChatAppColorScheme.current.onScreenBackground.copy(alpha = 0.5f),
                fontWeight = FontWeight.Normal,
            )
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ChatList(
    chats: List<ViewChatWithMessages>,
    actionListener: (MainAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val dropDownDisplayStrategy = remember {
        DropDownDisplayStrategy.OnLongPress<ViewChat, ViewChatAction>(
            displayRipple = true,
            onActionClick = { item, action ->
                actionListener(MainAction.ChatActionClicked(chat = item, action = action))
            },
            onPressListener = { chat ->
                actionListener(MainAction.ChatClicked(chat))
            }
        )
    }

    LazyColumn(modifier = modifier) {
        items(chats, key = { it.chat.id }) { chatWithMessage ->
            when (val chat = chatWithMessage.chat) {
                is ViewChat.Group -> GroupChatItem(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(color = LocalChatAppColorScheme.current.screenBackground)
                        //TODO: when lazy list is scrollable (takes full screen) this modifier hides the first item
                        .animateItemPlacement(),
                    chat = chat,
                    lastMessage = chatWithMessage.lastMessage,
                    unreadCount = chatWithMessage.numOfUnreadMessages,
                    dropDownDisplayStrategy = dropDownDisplayStrategy,
                )

                is ViewChat.Private -> PrivateChatItem(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(color = LocalChatAppColorScheme.current.screenBackground)
                        .animateItemPlacement(),
                    chat = chat,
                    lastMessage = chatWithMessage.lastMessage,
                    unreadCount = chatWithMessage.numOfUnreadMessages,
                    dropDownDisplayStrategy = dropDownDisplayStrategy,
                )
            }
        }
    }
}

@Composable
private fun GroupChatItem(
    chat: ViewChat.Group,
    lastMessage: ViewMessage?,
    unreadCount: Int,
    dropDownDisplayStrategy: DropDownDisplayStrategy<ViewChat, ViewChatAction>,
    modifier: Modifier = Modifier,
) {
    val actions = remember(chat.actions) {
        chat.actions.map { action ->
            DropdownMenuItemModel(
                text = UiText.Resource(action.nameStringRes),
                data = action,
            )
        }
    }

    ItemDropdownMenuContainer(
        modifier = modifier,
        item = chat,
        actions = actions,
        displayStrategy = dropDownDisplayStrategy,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
        ) {
            Row(
                modifier = Modifier
                    .padding(horizontal = ChatItemHorizontalPadding, vertical = ChatItemVerticalPadding)
                    .fillMaxWidth()
                    .wrapContentHeight(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                GroupChatImage(modifier = Modifier.size(ChatItemImageSize), chat = chat)

                Spacer(modifier = Modifier.width(ChatItemContentHorizontalPadding))
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        ChatNameText(modifier = Modifier.weight(1f), name = chat.name)

                        Spacer(modifier = Modifier.width(ChatItemContentHorizontalPadding))
                        MessageTimeText(time = lastMessage?.formattedTime ?: chat.formattedTime)
                    }

                    Spacer(modifier = Modifier.height(ChatItemContentVerticalPadding))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        if (lastMessage != null) {
                            val senderName =
                                lastMessage.user?.toMessageAuthorName(deviceAddress = lastMessage.userDeviceAddress)

                            MessageText(
                                modifier = Modifier.weight(1f),
                                senderName = senderName,
                                message = lastMessage,
                            )
                        } else {
                            Box(modifier = Modifier.weight(1f))
                        }

                        if (unreadCount > 0) {
                            Spacer(modifier = Modifier.width(ChatItemContentHorizontalPadding))
                            UnreadCountText(count = unreadCount)
                        }
                    }
                }
            }

            ChatDivider()
        }
    }
}

@Composable
private fun PrivateChatItem(
    chat: ViewChat.Private,
    lastMessage: ViewMessage?,
    unreadCount: Int,
    dropDownDisplayStrategy: DropDownDisplayStrategy<ViewChat, ViewChatAction>,
    modifier: Modifier = Modifier,
) {
    val actions = remember(chat.actions) {
        chat.actions.map { action ->
            DropdownMenuItemModel(
                text = UiText.Resource(action.nameStringRes),
                data = action,
            )
        }
    }
    ItemDropdownMenuContainer(
        modifier = modifier,
        item = chat,
        actions = actions,
        displayStrategy = dropDownDisplayStrategy,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
        ) {
            Row(
                modifier = Modifier
                    .padding(horizontal = ChatItemHorizontalPadding, vertical = ChatItemVerticalPadding)
                    .fillMaxWidth()
                    .wrapContentHeight(),
            ) {
                UserImage(
                    modifier = Modifier.size(ChatItemImageSize),
                    user = chat.user,
                    userDeviceAddress = chat.user.deviceAddress,
                )

                Spacer(modifier = Modifier.width(ChatItemContentHorizontalPadding))
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        ChatNameText(modifier = Modifier.weight(1f), name = chat.user.userName ?: "NAME NOT SET")

                        Spacer(modifier = Modifier.width(ChatItemContentHorizontalPadding))
                        MessageTimeText(time = lastMessage?.formattedTime ?: chat.formattedTime)
                    }

                    Spacer(modifier = Modifier.height(ChatItemContentVerticalPadding))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        if (lastMessage != null) {
                            val senderName =
                                if (lastMessage.isMine) stringResource(id = CoreUiR.string.chat_you) else null

                            MessageText(
                                modifier = Modifier.weight(1f),
                                senderName = senderName,
                                message = lastMessage,
                            )
                        } else {
                            Box(modifier = Modifier.weight(1f))
                        }

                        if (unreadCount > 0) {
                            Spacer(modifier = Modifier.width(ChatItemContentHorizontalPadding))
                            UnreadCountText(count = unreadCount)
                        }
                    }
                }
            }

            ChatDivider()
        }
    }
}

@Composable
private fun ChatNameText(name: String, modifier: Modifier = Modifier) {
    Text(
        modifier = modifier,
        text = name,
        textAlign = TextAlign.Start,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        style = TextStyle(
            fontSize = 17.sp,
            fontFamily = FontFamily.SansSerif,
            color = LocalChatAppColorScheme.current.onScreenBackground,
            fontWeight = FontWeight.Medium,
        )
    )
}

@Composable
private fun MessageTimeText(time: String, modifier: Modifier = Modifier) {
    Text(
        modifier = modifier,
        text = time,
        style = TextStyle(
            fontSize = 14.sp,
            fontFamily = FontFamily.SansSerif,
            color = LocalChatAppColorScheme.current.mainItemTimeText,
            fontWeight = FontWeight.Normal,
        )
    )
}

@Composable
private fun MessageText(message: ViewMessage, senderName: String?, modifier: Modifier = Modifier) {
    val annotatedText: AnnotatedString = when (message) {
        is ViewMessage.Plain -> {
            val messageContent = message.primaryContent()
            val contentText = messageContent.toShortDescription()
            val contentColor = messageContent.toShortColor()

            buildAnnotatedString {
                if (senderName != null) {
                    withStyle(style = SpanStyle(LocalChatAppColorScheme.current.onScreenBackground)) {
                        append("$senderName: ")
                    }
                }
                withStyle(style = SpanStyle(contentColor)) {
                    append(contentText)
                }
            }

        }

        is ViewMessage.GroupUpdate -> {
            buildAnnotatedString {
                withStyle(style = SpanStyle(LocalChatAppColorScheme.current.accent)) {
                    append(message.text())
                }
            }
        }
    }

    Text(
        modifier = modifier,
        text = annotatedText,
        textAlign = TextAlign.Start,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        style = TextStyle(
            fontSize = 15.sp,
            fontFamily = FontFamily.SansSerif,
            color = LocalChatAppColorScheme.current.mainItemMessageText,
            fontWeight = FontWeight.Normal,
        )
    )
}

@Composable
private fun UnreadCountText(count: Int, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .height(20.dp)
            .widthIn(min = 20.dp)
            .background(color = LocalChatAppColorScheme.current.accent, shape = CircleShape)
            .padding(horizontal = 4.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = count.toString(),
            style = TextStyle(
                fontSize = 12.sp,
                fontFamily = FontFamily.SansSerif,
                color = LocalChatAppColorScheme.current.onAccent,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
            ),
        )
    }
}

@Composable
private fun ChatDivider() {
    Box(
        modifier = Modifier
            .padding(
                start = ChatItemHorizontalPadding + ChatItemImageSize + ChatItemContentHorizontalPadding,
                end = ChatItemHorizontalPadding,
            )
            .fillMaxWidth()
            .height(1.dp)
            .background(color = LocalChatAppColorScheme.current.mainItemDivider),
    )
}
