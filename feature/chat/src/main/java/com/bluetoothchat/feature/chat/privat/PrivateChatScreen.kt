package com.bluetoothchat.feature.chat.privat

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bluetoothchat.core.permission.isGranted
import com.bluetoothchat.core.permission.rememberBluetoothPermissionsState
import com.bluetoothchat.core.permission.rememberWriteStoragePermissionState
import com.bluetoothchat.core.ui.ChatAppToolbar
import com.bluetoothchat.core.ui.ToolbarBackIconButton
import com.bluetoothchat.core.ui.ToolbarDropdownIconButton
import com.bluetoothchat.core.ui.ToolbarIconHorizontalPadding
import com.bluetoothchat.core.ui.ToolbarSubtitleText
import com.bluetoothchat.core.ui.ToolbarTitleText
import com.bluetoothchat.core.ui.components.ChatAppInfoSnackbar
import com.bluetoothchat.core.ui.components.ContentCrossFade
import com.bluetoothchat.core.ui.components.ExpandVerticallyAnimatedVisibility
import com.bluetoothchat.core.ui.components.ObserveViewResumedState
import com.bluetoothchat.core.ui.components.ScreenContainer
import com.bluetoothchat.core.ui.components.UserImage
import com.bluetoothchat.core.ui.components.dropdown.DropDownDisplayStrategy
import com.bluetoothchat.core.ui.components.dropdown.DropDownVisibilityHandler
import com.bluetoothchat.core.ui.model.ViewMessage
import com.bluetoothchat.core.ui.model.ViewMessageAction
import com.bluetoothchat.core.ui.mvi.action.toDropdownMenuItem
import com.bluetoothchat.core.ui.theme.LocalChatAppColorScheme
import com.bluetoothchat.core.ui.util.createEnableBluetoothIntent
import com.bluetoothchat.core.ui.util.createPickImageIntent
import com.bluetoothchat.core.ui.util.noRippleClickable
import com.bluetoothchat.core.ui.util.observeWithLifecycle
import com.bluetoothchat.core.ui.util.rememberLauncherEnableBluetooth
import com.bluetoothchat.core.ui.util.rememberLauncherForImage
import com.bluetoothchat.core.ui.util.showImageSavedSnackbar
import com.bluetoothchat.core.ui.util.showTextCopiedSnackbar
import com.bluetoothchat.feature.chat.common.ChatListVerticalPadding
import com.bluetoothchat.feature.chat.common.composables.ChatItem
import com.bluetoothchat.feature.chat.common.composables.footer.ChatFooter
import com.bluetoothchat.feature.chat.common.model.ViewChatItem
import com.bluetoothchat.feature.chat.common.model.toId
import com.bluetoothchat.feature.chat.privat.contract.PrivateChatAction
import com.bluetoothchat.feature.chat.privat.contract.PrivateChatEvent
import com.bluetoothchat.feature.chat.privat.contract.PrivateChatState
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.ramcosta.composedestinations.annotation.Destination
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import com.bluetoothchat.core.ui.R as CoreUiR

@Destination(navArgsDelegate = PrivateChatInputParams::class)
@Composable
fun PrivateChatScreen(navigator: PrivateChatNavigator) {
    val viewModel: PrivateChatViewModel = hiltViewModel()
    val viewState by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val lazyListState = rememberLazyListState()
    val inputFieldFocusRequester = remember { FocusRequester() }

    ObserveOneTimeEvents(
        viewModel = viewModel,
        navigator = navigator,
        snackbarHostState = snackbarHostState,
        listState = lazyListState,
        inputFieldFocusRequester = inputFieldFocusRequester,
    )
    ScreenContent(
        state = viewState,
        listState = lazyListState,
        snackbarHostState = snackbarHostState,
        actionListener = { viewModel.handleAction(it) },
        inputFieldFocusRequester = inputFieldFocusRequester,
    )
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun ObserveOneTimeEvents(
    viewModel: PrivateChatViewModel,
    navigator: PrivateChatNavigator,
    snackbarHostState: SnackbarHostState,
    listState: LazyListState,
    inputFieldFocusRequester: FocusRequester,
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val keyboardController = LocalSoftwareKeyboardController.current

    val bluetoothPermissionsState = rememberBluetoothPermissionsState(
        onStatusChanged = { permissionStatus ->
            viewModel.handleAction(PrivateChatAction.OnBluetoothPermissionResult(granted = permissionStatus.isGranted()))
        },
    )

    val writeStoragePermissionState = rememberWriteStoragePermissionState(
        onStatusChanged = { permissionStatus ->
            viewModel.handleAction(PrivateChatAction.OnWriteStoragePermissionResult(granted = permissionStatus.isGranted()))
        }
    )

    val enableBluetoothLauncher = rememberLauncherEnableBluetooth { enabled ->
        viewModel.handleAction(PrivateChatAction.OnEnableBluetoothResult(enabled = enabled))
    }

    val imagePickerLauncher = rememberLauncherForImage { resultUri ->
        viewModel.handleAction(PrivateChatAction.ExternalGalleryContentSelected(resultUri))
    }

    viewModel.oneTimeEvent.observeWithLifecycle(minActiveState = Lifecycle.State.RESUMED) { event ->
        when (event) {
            is PrivateChatEvent.NavigateBack -> navigator.navigateBack()
            is PrivateChatEvent.RequestBluetoothPermission -> bluetoothPermissionsState.launchMultiplePermissionRequest()
            is PrivateChatEvent.RequestWriteStoragePermission -> writeStoragePermissionState?.launchPermissionRequest()
            is PrivateChatEvent.RequestEnabledBluetooth -> enableBluetoothLauncher.launch(createEnableBluetoothIntent())
            is PrivateChatEvent.ShowDialog -> navigator.showDialog(event.params)
            is PrivateChatEvent.OpenExternalGalleryForImage -> imagePickerLauncher.launch(createPickImageIntent())
            is PrivateChatEvent.ScrollToLastMessage -> coroutineScope.launch {
                listState.animateScrollToItem(0)
            }

            is PrivateChatEvent.RequestInputFieldFocus -> {
                keyboardController?.show()
                inputFieldFocusRequester.requestFocus()
            }

            is PrivateChatEvent.ShowTextCopiedSnackbar -> {
                snackbarHostState.showTextCopiedSnackbar(
                    coroutineScope = coroutineScope,
                    context = context,
                )
            }

            is PrivateChatEvent.ShowImageSavedSnackbar -> {
                snackbarHostState.showImageSavedSnackbar(
                    coroutineScope = coroutineScope,
                    context = context,
                )
            }

            is PrivateChatEvent.NavigateToViewImageScreen -> navigator.navigateToViewImageScreen(
                chatId = event.chatId,
                messageId = event.messageId,
            )

            is PrivateChatEvent.NavigateToProfileScreen -> navigator.navigateToUserScreen(event.userDeviceAddress)
        }
    }

    navigator.OnDialogResult { viewModel.handleAction(PrivateChatAction.OnDialogResult(it)) }

    ObserveViewResumedState { viewModel.handleAction(PrivateChatAction.OnResumedStateChanged(it)) }
}

@Composable
private fun ScreenContent(
    state: PrivateChatState,
    listState: LazyListState,
    snackbarHostState: SnackbarHostState,
    actionListener: (PrivateChatAction) -> Unit,
    inputFieldFocusRequester: FocusRequester,
) {
    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo }
            .map { it.lastOrNull()?.key }
            .distinctUntilChanged()
            .collect { itemId ->
                actionListener(PrivateChatAction.OnFirstVisibleItemChanged(itemId = itemId as? String))
            }
    }

    val screenHasVisiblePopup = remember { mutableStateOf(false) }
    ScreenContainer(
        topAppBar = { PrivateChatToolbar(state = state, actionListener = actionListener) }
    ) {
        ContentCrossFade(
            modifier = Modifier
                .fillMaxSize()
                .background(color = LocalChatAppColorScheme.current.chatScreenBackground),
            targetState = state,
            contentKey = { it::class },
        ) { animatedState ->
            when (animatedState) {
                is PrivateChatState.Loading -> {
                    //Show some progress
                }

                is PrivateChatState.Loaded -> {
                    LoadedScreenContent(
                        state = animatedState,
                        listState = listState,
                        actionListener = actionListener,
                        inputFieldFocusRequester = inputFieldFocusRequester,
                        screenHasVisiblePopup = screenHasVisiblePopup,
                    )
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .navigationBarsPadding()
                .imePadding()
                .padding(16.dp)
                .align(Alignment.BottomCenter),
        ) {
            ChatAppInfoSnackbar(it)
        }
    }

    if (screenHasVisiblePopup.value) {
        BackHandler { screenHasVisiblePopup.value = false }
        Box(modifier = Modifier
            .fillMaxSize()
            .noRippleClickable { screenHasVisiblePopup.value = false })
    }
}

@Composable
private fun PrivateChatToolbar(state: PrivateChatState, actionListener: (PrivateChatAction) -> Unit) {
    ChatAppToolbar {
        Spacer(modifier = Modifier.width(ToolbarIconHorizontalPadding))
        ToolbarBackIconButton(navigateUp = { actionListener(PrivateChatAction.BackButtonClicked) })
        Spacer(modifier = Modifier.width(ToolbarIconHorizontalPadding))

        (state as? PrivateChatState.Loaded).let { loadedState ->
            ContentCrossFade(
                modifier = Modifier.fillMaxSize(),
                targetState = loadedState,
                contentKey = { it != null },
            ) { animatedState ->
                if (animatedState != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .noRippleClickable {
                                actionListener(PrivateChatAction.UserClicked(user = animatedState.chat.user))
                            },
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        UserImage(
                            modifier = Modifier.size(40.dp),
                            user = animatedState.chat.user,
                            userDeviceAddress = animatedState.chat.user.deviceAddress,
                            displayConnectedIndicator = false,
                        )
                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            ToolbarTitleText(
                                text = animatedState.chat.user.userName ?: animatedState.chat.user.deviceAddress
                            )
                            ExpandVerticallyAnimatedVisibility(visible = animatedState.chat.user.isConnected) {
                                ToolbarSubtitleText(text = stringResource(id = CoreUiR.string.connected))
                            }
                        }
                        Spacer(modifier = Modifier.width(ToolbarIconHorizontalPadding))

                        val actions = remember(animatedState.chat.actions) {
                            animatedState.chat.actions.map { it.toDropdownMenuItem() }
                        }
                        ToolbarDropdownIconButton(
                            actions = actions,
                            buttonClickListener = { },
                            actionClickListener = { actionListener(PrivateChatAction.ChatActionClicked(action = it)) }
                        )
                        Spacer(modifier = Modifier.width(ToolbarIconHorizontalPadding))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun LoadedScreenContent(
    state: PrivateChatState.Loaded,
    listState: LazyListState,
    actionListener: (PrivateChatAction) -> Unit,
    inputFieldFocusRequester: FocusRequester,
    screenHasVisiblePopup: MutableState<Boolean>,
) {
    val dropDownDisplayStrategy = remember {
        DropDownDisplayStrategy.OnPress<ViewChatItem, ViewMessageAction>(
            displayRipple = false,
            onActionClick = { item, action ->
                if (item is ViewChatItem.Message && item.message is ViewMessage.Plain) {
                    actionListener(PrivateChatAction.MessageActionClicked(action = action, message = item.message))
                }
            },
            visibilityHandler = DropDownVisibilityHandler.Custom(screenHasVisiblePopup = screenHasVisiblePopup),
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            state = listState,
            reverseLayout = true,
            contentPadding = PaddingValues(vertical = ChatListVerticalPadding)
        ) {
            //A hacky way of fixing first item addition animation
            item(key = "0") { }

            items(items = state.items, key = { it.toId() }) { item ->
                ChatItem(
                    modifier = Modifier.animateItemPlacement(),
                    item = item,
                    showOtherUserImages = false,
                    userClickListener = { actionListener(PrivateChatAction.UserClicked(it)) },
                    imageClickListener = { actionListener(PrivateChatAction.MessageImageClicked(it)) },
                    dropDownDisplayStrategy = dropDownDisplayStrategy,
                )
            }
        }

        ChatFooter(
            modifier = Modifier
                .fillMaxWidth(),
            state = state.footer,
            quotedMessage = state.quotedMessage,
            actionListener = { actionListener(PrivateChatAction.FooterActionClicked(it)) },
            focusRequester = inputFieldFocusRequester,
        )
    }
}
