package com.bluetoothchat.feature.chat.group

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.bluetoothchat.core.bluetooth.BtServiceManager
import com.bluetoothchat.core.bluetooth.message.ConnectionState
import com.bluetoothchat.core.bluetooth.message.manager.CommunicationManagerImpl
import com.bluetoothchat.core.bluetooth.scanner.BtScanner
import com.bluetoothchat.core.db.datasource.ChatDataSource
import com.bluetoothchat.core.db.datasource.MessageDataSource
import com.bluetoothchat.core.db.datasource.UserDataSource
import com.bluetoothchat.core.dispatcher.DispatcherManager
import com.bluetoothchat.core.domain.model.Chat
import com.bluetoothchat.core.domain.model.Message
import com.bluetoothchat.core.domain.model.MessageContent
import com.bluetoothchat.core.domain.model.User
import com.bluetoothchat.core.filemanager.file.FileState
import com.bluetoothchat.core.filemanager.image.ImageProcessor
import com.bluetoothchat.core.permission.PermissionManager
import com.bluetoothchat.core.session.Session
import com.bluetoothchat.core.ui.R
import com.bluetoothchat.core.ui.components.dialog.model.DialogInputParams
import com.bluetoothchat.core.ui.components.dialog.model.InvalidImageDialogInputParams
import com.bluetoothchat.core.ui.delegate.ChatActionHandlerDelegate
import com.bluetoothchat.core.ui.delegate.ChatActionHandlerDelegateEvent
import com.bluetoothchat.core.ui.model.UiText
import com.bluetoothchat.core.ui.model.mapper.ViewMessageMapper
import com.bluetoothchat.core.ui.model.mapper.ViewUserMapper
import com.bluetoothchat.core.ui.model.mapper.chat.ViewGroupChatMapper
import com.bluetoothchat.core.ui.mvi.MviViewModel
import com.bluetoothchat.core.ui.mvi.action.handler.ActionHandlerDelegateAction
import com.bluetoothchat.core.ui.util.TimeFormatType
import com.bluetoothchat.core.ui.util.TimeFormatter
import com.bluetoothchat.core.ui.util.combine
import com.bluetoothchat.feature.chat.common.MessageActionHandlerDelegate
import com.bluetoothchat.feature.chat.common.MessageActionResult
import com.bluetoothchat.feature.chat.common.MessagePagingDelegate
import com.bluetoothchat.feature.chat.common.composables.footer.ChatFooterAction
import com.bluetoothchat.feature.chat.common.composables.footer.ChatFooterState
import com.bluetoothchat.feature.chat.common.model.ViewChatItem
import com.bluetoothchat.feature.chat.destinations.GroupChatScreenDestination
import com.bluetoothchat.feature.chat.group.contract.GroupChatAction
import com.bluetoothchat.feature.chat.group.contract.GroupChatEvent
import com.bluetoothchat.feature.chat.group.contract.GroupChatState
import com.bluetoothchat.feature.chat.image.saver.SaveImageResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class GroupChatViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val btServiceManager: BtServiceManager,
    private val dispatcherManager: DispatcherManager,
    private val chatDataSource: ChatDataSource,
    private val userDataSource: UserDataSource,
    private val messageDataSource: MessageDataSource,
    private val communicationManager: CommunicationManagerImpl,
    private val session: Session,
    private val imageProcessor: ImageProcessor,
    private val timeFormatter: TimeFormatter,
    private val userMapper: ViewUserMapper,
    private val messageMapper: ViewMessageMapper,
    private val groupChatMapper: ViewGroupChatMapper,
    private val chatActionHandlerDelegate: ChatActionHandlerDelegate,
    private val messageActionHandlerDelegate: MessageActionHandlerDelegate,
    private val analyticsClient: GroupChatAnalyticsClient,
    private val permissionManager: PermissionManager,
    private val scanner: BtScanner,
) : MviViewModel<GroupChatState, GroupChatAction, GroupChatEvent>(GroupChatState.Loading) {

    private val inputParams = getInputParams(savedStateHandle)

    private val messagePagingDelegate = MessagePagingDelegate(messageDataSource = messageDataSource)

    init {
        viewModelScope.launch(dispatcherManager.default) {
            val chat = chatDataSource.getGroupChatById(inputParams.chatId)
            if (chat != null) {
                analyticsClient.reportScreenShown(
                    chat = chat,
                    source = inputParams.source,
                    messagesCount = messageDataSource.getCount(chatId = inputParams.chatId),
                )
            }
        }
        observeChatState()
        observeAndMarkMessagesAsRead()
        observeChatActionDelegateEvent()
    }

    override fun handleAction(action: GroupChatAction) {
        when (action) {
            is GroupChatAction.BackButtonClicked -> handleBackButtonClicked(action)
            is GroupChatAction.OnResumedStateChanged -> handleResumedStateChanged(action)
            is GroupChatAction.OnDialogResult -> handleOnDialogResult(action)
            is GroupChatAction.OnFirstVisibleItemChanged -> handleOnFirstVisibleItemChanged(action)
            is GroupChatAction.ChatImageClicked -> handleChatImageClicked(action)
            is GroupChatAction.FooterActionClicked -> handleFooterActionClicked(action)
            is GroupChatAction.UserClicked -> handleUserClicked(action)
            is GroupChatAction.MessageImageClicked -> handleMessageImageClicked(action)
            is GroupChatAction.MessageActionClicked -> handleMessageActionClicked(action)
            is GroupChatAction.ChatActionClicked -> handleChatActionClicked(action)
            is GroupChatAction.ExternalGalleryContentSelected -> handleExternalGalleryContentSelected(action)
            is GroupChatAction.OnBluetoothPermissionResult -> handleBluetoothPermissionResult(action)
            is GroupChatAction.OnWriteStoragePermissionResult -> handleWriteStoragePermissionResult(action)
            is GroupChatAction.OnEnableBluetoothResult -> handleEnableBluetoothResult(action)
        }
    }

    private fun observeChatActionDelegateEvent() {
        chatActionHandlerDelegate.oneTimeEvent
            .onEach { event ->
                when (event) {
                    is ChatActionHandlerDelegateEvent.CloseChatScreen -> sendEvent { GroupChatEvent.NavigateBack }
                    is ChatActionHandlerDelegateEvent.ShowDialog ->
                        sendEvent { GroupChatEvent.ShowDialog(params = event.params) }
                }
            }
            .flowOn(dispatcherManager.default)
            .launchIn(viewModelScope)
    }

    private fun observeChatState() {
        combine(
            chatDataSource.observeGroupChatById(inputParams.chatId),
            messagePagingDelegate.observeChatMessages(chatId = inputParams.chatId),
            userDataSource.observeAll(),
            communicationManager.connectionStateFlow,
            communicationManager.observeChatFilesBeingDownloaded(inputParams.chatId),
            communicationManager.observeUserFilesBeingDownloaded(),
        ) { chat, messages, allUsers, connectionState, filesBeingDownloaded, _ ->
            Log.v("BluetoothScanner", "GroupVm, combine")
            GroupChatDataHolder(
                chat = chat,
                messages = messages,
                allUsers = allUsers,
                connectionState = connectionState,
                filesBeingDownloaded = filesBeingDownloaded
            )
        }
            .conflate()
            .onEach { holder ->
                val chat = holder.chat
                val messages = holder.messages
                val allUsers = holder.allUsers
                val connectionState = holder.connectionState
                val filesBeingDownloaded = holder.filesBeingDownloaded

                if (chat != null) {
                    val hostConnectionState = if (session.isCurrentUser(chat.hostDeviceAddress)) {
                        ConnectionState.CONNECTED
                    } else {
                        connectionState[chat.hostDeviceAddress] ?: ConnectionState.DISCONNECTED
                    }

                    //Only show direct connections in the group chat if user is the host
                    //otherwise they are irrelevant (user might be connected to you but not to chat's host)
                    val viewConnectedDevices = if (session.isCurrentUser(chat.hostDeviceAddress)) {
                        connectionState.filterValues { it == ConnectionState.CONNECTED }.keys.toList()
                    } else {
                        emptyList()
                    }

                    //For the actions if the user is not the host we need to know whether it is connected to the host
                    val actionsConnectedDevices = if (session.isCurrentUser(chat.hostDeviceAddress)) {
                        connectionState.filterValues { it == ConnectionState.CONNECTED }
                    } else {
                        connectionState.filter { it.key == chat.hostDeviceAddress && it.value == ConnectionState.CONNECTED }
                    }.keys.toList()

                    val canSendMessages = hostConnectionState == ConnectionState.CONNECTED && chat.exists
                            && chat.users.any { session.isCurrentUser(it.deviceAddress) }

                    val currentUser = userMapper.map(user = session.getUser(), connectedDevices = emptyList())
                    val viewChat = groupChatMapper.map(chat = chat, connectedDevices = actionsConnectedDevices)

                    val allViewUsers = allUsers.map { user ->
                        userMapper.map(user = user, connectedDevices = viewConnectedDevices)
                    }

                    val chatItems = mutableListOf<ViewChatItem>()
                    for (i in messages.indices) {
                        val currentMessage = messages.get(i)
                        val prevMessage = messages.getOrNull(i + 1)
                        val prevUserDifferent = prevMessage?.userDeviceAddress != currentMessage.userDeviceAddress
                        val prevMessagePlain = prevMessage is Message.Plain
                        val prevDateDifferent = prevMessage != null && timeFormatter.areTheSameDay(
                            timestamp1 = currentMessage.timestamp,
                            timestamp2 = prevMessage.timestamp,
                        )
                        val isMine = currentMessage.userDeviceAddress == currentUser.deviceAddress
                        val displayUserInfo = !isMine && (prevUserDifferent || !prevMessagePlain)

                        val messageItem = messageMapper.map(
                            chatId = inputParams.chatId,
                            message = currentMessage,
                            filesBeingDownloaded = filesBeingDownloaded,
                            currentUser = currentUser,
                            displayUserInfo = displayUserInfo,
                            allUsers = allViewUsers,
                            canSendMessages = canSendMessages,
                        )

                        chatItems.add(
                            ViewChatItem.Message(
                                message = messageItem,
                                extendedTopPadding = prevUserDifferent,
                            )
                        )
                        if (!prevDateDifferent) {
                            chatItems.add(
                                ViewChatItem.DateHeader(
                                    timeFormatter.formatTimeDate(
                                        timestamp = currentMessage.timestamp,
                                        type = TimeFormatType.TODAY_OR_DATE,
                                    )
                                )
                            )
                        }
                    }

                    val footer = when {
                        !chat.exists -> ChatFooterState.Info(
                            message = UiText.Resource(R.string.chat_input_group_doesnt_exist),
                        )

                        !chat.users.any { session.isCurrentUser(it.deviceAddress) } -> ChatFooterState.Info(
                            message = UiText.Resource(R.string.chat_input_group_no_longer_member),
                        )

                        hostConnectionState == ConnectionState.DISCONNECTED -> ChatFooterState.InfoWithButton(
                            infoText = UiText.Resource(R.string.chat_input_group_not_connected_message),
                            buttonId = BUTTON_ID_CONNECT,
                            buttonEnabled = true,
                            buttonText = UiText.Resource(R.string.chat_input_button_connect),
                        )

                        hostConnectionState == ConnectionState.CONNECTING -> ChatFooterState.InfoWithButton(
                            infoText = UiText.Resource(R.string.chat_input_connecting_message),
                            buttonId = 0,
                            buttonEnabled = false,
                            buttonText = UiText.Resource(R.string.chat_input_button_connect),
                        )

                        else -> ChatFooterState.InputField
                    }

                    val loadedState = state.value as? GroupChatState.Loaded
                    setState {
                        GroupChatState.Loaded(
                            chat = viewChat,
                            items = chatItems,
                            quotedMessage = loadedState?.quotedMessage,
                            footer = footer,
                            isConnectActionPending = loadedState?.isConnectActionPending ?: false,
                        )
                    }
                } else {
                    //Close screen?
                }
                Log.v("BluetoothScanner", "GroupVm, onEach mapping END")

            }
            .flowOn(dispatcherManager.default)
            .launchIn(viewModelScope)
    }

    private fun observeAndMarkMessagesAsRead() {
        messageDataSource.observeUnreadCount(inputParams.chatId)
            .distinctUntilChanged()
            .onEach {
                messageDataSource.markAllAsRead(inputParams.chatId)
            }
            .launchIn(viewModelScope)
    }

    private fun handleBackButtonClicked(action: GroupChatAction.BackButtonClicked) {
        sendEvent { GroupChatEvent.NavigateBack }
    }

    private fun handleResumedStateChanged(action: GroupChatAction.OnResumedStateChanged) {
        if (action.isResumed) {
            communicationManager.onChatScreenOpened(chatId = inputParams.chatId)
        } else {
            communicationManager.onChatScreenClosed(chatId = inputParams.chatId)
        }
    }

    private fun handleOnDialogResult(action: GroupChatAction.OnDialogResult) {
        viewModelScope.launch(dispatcherManager.default) {
            val dialogId = action.result.dialogId
            when {
                chatActionHandlerDelegate.shouldHandleDialogResult(dialogId) ->
                    chatActionHandlerDelegate.handleAction(
                        ActionHandlerDelegateAction.OnDialogResult(result = action.result)
                    )
            }
        }
    }

    private fun handleOnFirstVisibleItemChanged(action: GroupChatAction.OnFirstVisibleItemChanged) {
        viewModelScope.launch(dispatcherManager.default) {
            if (action.itemId != null) {
                val isValidMessageId = action.itemId.length == 36
                if (isValidMessageId) {
                    messagePagingDelegate.updateWindowStartMessageId(messageId = action.itemId)
                }
            }
        }
    }

    private fun handleFooterActionClicked(action: GroupChatAction.FooterActionClicked) {
        val loadedState = state.value as? GroupChatState.Loaded ?: return

        viewModelScope.launch(dispatcherManager.default) {
            when (action.action) {
                is ChatFooterAction.SendMessageClicked -> {
                    sendMessage(content = MessageContent.Text(action.action.text))
                }

                is ChatFooterAction.ButtonClicked -> {
                    when (action.action.id) {
                        BUTTON_ID_CONNECT -> {
                            analyticsClient.reportConnectClicked(source = inputParams.source)
                            setState { loadedState.copy(isConnectActionPending = true) }

                            if (permissionManager.bluetoothPermissionsGranted()) {
                                onBluetoothPermissionGranted()
                            } else {
                                sendEvent { GroupChatEvent.RequestBluetoothPermission }
                            }
                        }
                    }
                }

                is ChatFooterAction.FileClicked -> {
                    sendEvent { GroupChatEvent.OpenExternalGalleryForImage }
                }

                is ChatFooterAction.ClearReplyClicked -> {
                    setState { loadedState.copy(quotedMessage = null) }
                }
            }
        }
    }

    private fun handleChatImageClicked(action: GroupChatAction.ChatImageClicked) {
        sendEvent { GroupChatEvent.NavigateToChatInfoScreenScreen(inputParams.chatId) }
    }

    private fun handleUserClicked(action: GroupChatAction.UserClicked) {
        sendEvent { GroupChatEvent.NavigateToProfileScreen(userDeviceAddress = action.user.deviceAddress) }
    }

    private fun handleMessageImageClicked(action: GroupChatAction.MessageImageClicked) {
        sendEvent {
            GroupChatEvent.NavigateToViewImageScreen(
                chatId = inputParams.chatId,
                messageId = action.message.id,
            )
        }
    }

    private fun handleExternalGalleryContentSelected(action: GroupChatAction.ExternalGalleryContentSelected) {
        viewModelScope.launch {
            val image = imageProcessor.saveChatImage(uri = action.uri, chatId = inputParams.chatId).getOrNull()

            if (image != null) {
                sendMessage(content = image)
            } else {
                sendEvent { GroupChatEvent.ShowDialog(InvalidImageDialogInputParams) }
            }
        }
    }

    private fun handleMessageActionClicked(action: GroupChatAction.MessageActionClicked) {
        val loadedState = state.value as? GroupChatState.Loaded ?: return
        viewModelScope.launch(dispatcherManager.default) {
            val actionResult = messageActionHandlerDelegate.handleAction(message = action.message, action.action)
            when (actionResult) {
                is MessageActionResult.SaveImageAttempted -> {
                    when (actionResult.result) {
                        is SaveImageResult.Success -> {
                            sendEvent { GroupChatEvent.ShowImageSavedSnackbar }
                        }

                        is SaveImageResult.Error -> {
                            if (actionResult.result is SaveImageResult.Error.NoWriteStoragePermission) {
                                sendEvent { GroupChatEvent.RequestWriteStoragePermission }
                            }
                        }
                    }
                }

                is MessageActionResult.DisplayReply -> {
                    setState { loadedState.copy(quotedMessage = action.message) }
                    sendEvent { GroupChatEvent.RequestInputFieldFocus }
                }

                is MessageActionResult.TextCopied -> {
                    sendEvent { GroupChatEvent.ShowTextCopiedSnackbar }
                }
            }
        }
    }

    private fun handleChatActionClicked(action: GroupChatAction.ChatActionClicked) {
        viewModelScope.launch(dispatcherManager.default) {
            chatActionHandlerDelegate.handleAction(ActionHandlerDelegateAction.ActionClicked(action = action.action))
        }
    }

    private fun handleBluetoothPermissionResult(action: GroupChatAction.OnBluetoothPermissionResult) {
        viewModelScope.launch(dispatcherManager.default) {
            if (action.granted) {
                analyticsClient.onBluetoothPermissionGranted()
                onBluetoothPermissionGranted()
            } else {
                analyticsClient.onBluetoothPermissionDenied()
            }
        }
    }

    private suspend fun onBluetoothPermissionGranted() {
        val loadedState = state.value as? GroupChatState.Loaded ?: return

        if (loadedState.isConnectActionPending) {
            if (communicationManager.isBluetoothEnabled()) {
                btServiceManager.ensureStarted()
                connect()
            } else {
                sendEvent { GroupChatEvent.RequestEnabledBluetooth }
            }
        }
    }

    private fun handleWriteStoragePermissionResult(action: GroupChatAction.OnWriteStoragePermissionResult) {
        //TODO: Save image? (needs action caching)
    }

    private fun handleEnableBluetoothResult(action: GroupChatAction.OnEnableBluetoothResult) {
        val loadedState = state.value as? GroupChatState.Loaded ?: return

        viewModelScope.launch(dispatcherManager.default) {
            if (action.enabled) {
                analyticsClient.reportBluetoothEnabled()
                if (loadedState.isConnectActionPending) {
                    btServiceManager.ensureStarted()
                    connect()
                }
            }
        }
    }

    private suspend fun connect() {
        val loadedState = state.value as? GroupChatState.Loaded ?: return

        if (scanner.isDevicePaired(address = loadedState.chat.hostDeviceAddress)) {
            val connected = communicationManager.connect(deviceAddress = loadedState.chat.hostDeviceAddress)

            if (!connected) {
                analyticsClient.reportConnectErrorDialogShown()

                sendEvent {
                    GroupChatEvent.ShowDialog(
                        params = DialogInputParams.TextDialog(
                            title = UiText.Resource(R.string.dialog_error),
                            message = UiText.Resource(R.string.connection_error_dialog_message),
                        )
                    )
                }
            }
        } else {
            sendEvent {
                GroupChatEvent.ShowDialog(
                    params = DialogInputParams.TextDialog(
                        title = UiText.Resource(R.string.dialog_error),
                        message = UiText.Resource(R.string.device_not_paired_anymore_message),
                    )
                )
            }
        }
    }

    private suspend fun sendMessage(content: MessageContent) {
        val loadedState = state.value as? GroupChatState.Loaded ?: return

        communicationManager.sendGroupChatMessage(
            chatId = loadedState.chat.id,
            content = content,
            quotedMessageId = loadedState.quotedMessage?.id,
        )

        setState { loadedState.copy(quotedMessage = null) }
        sendEvent { GroupChatEvent.ScrollToLastMessage }
    }

    private data class GroupChatDataHolder(
        val chat: Chat.Group?,
        val messages: List<Message>,
        val allUsers: List<User>,
        val connectionState: Map<String, ConnectionState>,
        val filesBeingDownloaded: List<FileState.Downloading>,
    )

    private companion object {
        const val BUTTON_ID_CONNECT = 0

        fun getInputParams(savedStateHandle: SavedStateHandle) =
            requireNotNull(GroupChatScreenDestination.argsFrom(savedStateHandle))
    }
}
