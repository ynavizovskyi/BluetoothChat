package com.bluetoothchat.feature.chat.privat

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.bluetoothchat.core.bluetooth.BtServiceManager
import com.bluetoothchat.core.bluetooth.message.ConnectionState
import com.bluetoothchat.core.bluetooth.message.manager.CommunicationManagerImpl
import com.bluetoothchat.core.bluetooth.scanner.BtScanner
import com.bluetoothchat.core.db.datasource.ChatDataSource
import com.bluetoothchat.core.db.datasource.MessageDataSource
import com.bluetoothchat.core.dispatcher.DispatcherManager
import com.bluetoothchat.core.domain.model.Chat
import com.bluetoothchat.core.domain.model.Message
import com.bluetoothchat.core.domain.model.MessageContent
import com.bluetoothchat.core.filemanager.file.FileState
import com.bluetoothchat.core.filemanager.image.ImageProcessor
import com.bluetoothchat.core.permission.PermissionManager
import com.bluetoothchat.core.session.Session
import com.bluetoothchat.core.ui.components.dialog.model.DialogInputParams
import com.bluetoothchat.core.ui.components.dialog.model.InvalidImageDialogInputParams
import com.bluetoothchat.core.ui.delegate.ChatActionHandlerDelegate
import com.bluetoothchat.core.ui.delegate.ChatActionHandlerDelegateEvent
import com.bluetoothchat.core.ui.model.UiText
import com.bluetoothchat.core.ui.model.mapper.ViewMessageMapper
import com.bluetoothchat.core.ui.model.mapper.ViewUserMapper
import com.bluetoothchat.core.ui.model.mapper.chat.ViewPrivateChatMapper
import com.bluetoothchat.core.ui.model.toDomain
import com.bluetoothchat.core.ui.mvi.MviViewModel
import com.bluetoothchat.core.ui.mvi.action.handler.ActionHandlerDelegateAction
import com.bluetoothchat.core.ui.util.TimeFormatType
import com.bluetoothchat.core.ui.util.TimeFormatter
import com.bluetoothchat.feature.chat.common.MessageActionHandlerDelegate
import com.bluetoothchat.feature.chat.common.MessageActionResult
import com.bluetoothchat.feature.chat.common.MessagePagingDelegate
import com.bluetoothchat.feature.chat.common.composables.footer.ChatFooterAction
import com.bluetoothchat.feature.chat.common.composables.footer.ChatFooterState
import com.bluetoothchat.feature.chat.common.model.ViewChatItem
import com.bluetoothchat.feature.chat.destinations.PrivateChatScreenDestination
import com.bluetoothchat.feature.chat.image.saver.SaveImageResult
import com.bluetoothchat.feature.chat.privat.contract.PrivateChatAction
import com.bluetoothchat.feature.chat.privat.contract.PrivateChatEvent
import com.bluetoothchat.feature.chat.privat.contract.PrivateChatState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.bluetoothchat.core.ui.R as CoreUiR

@HiltViewModel
internal class PrivateChatViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val btServiceManager: BtServiceManager,
    private val dispatcherManager: DispatcherManager,
    private val chatDataSource: ChatDataSource,
    private val messageDataSource: MessageDataSource,
    private val session: Session,
    private val communicationManager: CommunicationManagerImpl,
    private val imageProcessor: ImageProcessor,
    private val timeFormatter: TimeFormatter,
    private val userMapper: ViewUserMapper,
    private val messageMapper: ViewMessageMapper,
    private val privateChatMapper: ViewPrivateChatMapper,
    private val chatActionHandlerDelegate: ChatActionHandlerDelegate,
    private val messageActionHandlerDelegate: MessageActionHandlerDelegate,
    private val analyticsClient: PrivateChatAnalyticsClient,
    private val permissionManager: PermissionManager,
    private val scanner: BtScanner,
) : MviViewModel<PrivateChatState, PrivateChatAction, PrivateChatEvent>(PrivateChatState.Loading) {

    private val messagePagingDelegate = MessagePagingDelegate(messageDataSource = messageDataSource)

    private val inputParams = getInputParams(savedStateHandle)

    init {
        viewModelScope.launch(dispatcherManager.default) {
            analyticsClient.reportScreenShown(
                source = inputParams.source,
                messagesCount = messageDataSource.getCount(chatId = inputParams.chatId),
            )
        }
        observeChatState()
        observeAndMarkMessagesAsRead()
        observeChatActionDelegateEvent()
    }

    override fun handleAction(action: PrivateChatAction) {
        when (action) {
            is PrivateChatAction.BackButtonClicked -> handleBackButtonClicked(action)
            is PrivateChatAction.OnResumedStateChanged -> handleResumedStateChanged(action)
            is PrivateChatAction.OnDialogResult -> handleOnDialogResult(action)
            is PrivateChatAction.OnFirstVisibleItemChanged -> handleOnFirstVisibleItemChanged(action)
            is PrivateChatAction.UserClicked -> handleUserClicked(action)
            is PrivateChatAction.MessageImageClicked -> handleMessageImageClicked(action)
            is PrivateChatAction.MessageActionClicked -> handleMessageActionClicked(action)
            is PrivateChatAction.ChatActionClicked -> handleChatActionClicked(action)
            is PrivateChatAction.FooterActionClicked -> handleFooterActionClicked(action)
            is PrivateChatAction.ExternalGalleryContentSelected -> handleExternalGalleryContentSelected(action)
            is PrivateChatAction.OnBluetoothPermissionResult -> handleBluetoothPermissionResult(action)
            is PrivateChatAction.OnWriteStoragePermissionResult -> handleWriteStoragePermissionResult(action)
            is PrivateChatAction.OnEnableBluetoothResult -> handleEnableBluetoothResult(action)
        }
    }

    private fun observeChatActionDelegateEvent() {
        chatActionHandlerDelegate.oneTimeEvent
            .onEach { event ->
                when (event) {
                    is ChatActionHandlerDelegateEvent.CloseChatScreen -> sendEvent { PrivateChatEvent.NavigateBack }
                    is ChatActionHandlerDelegateEvent.ShowDialog -> {
                        sendEvent { PrivateChatEvent.ShowDialog(params = event.params) }
                    }
                }
            }
            .flowOn(dispatcherManager.default)
            .launchIn(viewModelScope)
    }

    private fun observeChatState() {
        combine(
            chatDataSource.observePrivateChatById(inputParams.chatId),
//            messageDataSource.observeAll(inputParams.chatId),
            messagePagingDelegate.observeChatMessages(chatId = inputParams.chatId),
            communicationManager.connectionStateFlow,
            communicationManager.observeChatFilesBeingDownloaded(inputParams.chatId),
            communicationManager.observeUserFilesBeingDownloaded(),
        ) { chat, messages, connectionState, filesBeingDownloaded, _ ->
            Log.v("BluetoothScanner", "PrivateVm, combine")

            PrivateChatDataHolder(
                chat = chat,
                messages = messages,
                connectionState = connectionState,
                filesBeingDownloaded = filesBeingDownloaded
            )
        }
            .conflate()
            .onEach { holder ->
                val kk = System.currentTimeMillis()
                val chat = holder.chat
                val messages = holder.messages
                val connectionState = holder.connectionState
                val filesBeingDownloaded = holder.filesBeingDownloaded
                Log.v(
                    "BluetoothScanner",
                    "PrivateVm, onEach : ${filesBeingDownloaded.map { it.toString() + "  " }}"
                )

                if (chat != null) {
                    val userConnectionState = connectionState[chat.user.deviceAddress] ?: ConnectionState.DISCONNECTED
                    val canSendMessages = userConnectionState == ConnectionState.CONNECTED && chat.exists

                    val connectedDevices =
                        connectionState.filterValues { it == ConnectionState.CONNECTED }.keys.toList()
                    val currentViewUser = userMapper.map(user = session.getUser(), connectedDevices = emptyList())
                    val viewChat = privateChatMapper.map(chat = chat, connectedDevices = connectedDevices)
                    val viewUser = viewChat.user
                    //Needed for mapping of complex content types
                    val allUsers = listOf(currentViewUser, viewUser)

                    val chatItems = mutableListOf<ViewChatItem>()
                    for (i in messages.indices) {
                        val currentMessage = messages.get(i)
                        val prevMessage = messages.getOrNull(i + 1)
                        val prevUserDifferent = prevMessage?.userDeviceAddress != currentMessage.userDeviceAddress
                        val prevDateDifferent = prevMessage != null && timeFormatter.areTheSameDay(
                            timestamp1 = currentMessage.timestamp,
                            timestamp2 = prevMessage.timestamp,
                        )

                        val messageItem = messageMapper.map(
                            chatId = inputParams.chatId,
                            message = currentMessage,
                            filesBeingDownloaded = filesBeingDownloaded,
                            currentUser = currentViewUser,
                            allUsers = allUsers,
                            canSendMessages = canSendMessages,
                        )

                        chatItems.add(
                            ViewChatItem.Message(message = messageItem, extendedTopPadding = prevUserDifferent)
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
                        userConnectionState == ConnectionState.DISCONNECTED -> ChatFooterState.InfoWithButton(
                            infoText = UiText.Resource(CoreUiR.string.chat_input_private_not_connected_message),
                            buttonId = BUTTON_ID_CONNECT,
                            buttonEnabled = true,
                            buttonText = UiText.Resource(CoreUiR.string.chat_input_button_connect),
                        )

                        userConnectionState == ConnectionState.CONNECTING -> ChatFooterState.InfoWithButton(
                            infoText = UiText.Resource(CoreUiR.string.chat_input_connecting_message),
                            buttonId = 0,
                            buttonEnabled = false,
                            buttonText = UiText.Resource(CoreUiR.string.chat_input_button_connect),
                        )

                        !chat.exists -> ChatFooterState.InfoWithButton(
                            infoText = UiText.Resource(CoreUiR.string.chat_input_private_doesnt_exist),
                            buttonId = BUTTON_ID_RESTART_CHAT,
                            buttonEnabled = true,
                            buttonText = UiText.Resource(CoreUiR.string.chat_input_button_restart),
                        )

                        else -> ChatFooterState.InputField
                    }

                    val loadedState = state.value as? PrivateChatState.Loaded
                    Log.v("ldkfjslkdkdjhkjh", "${System.currentTimeMillis() - kk}")
                    setState {
                        PrivateChatState.Loaded(
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
                Log.v("BluetoothScanner", "PrivateVm, onEach mapping END")

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
            .flowOn(dispatcherManager.default)
            .launchIn(viewModelScope)
    }

    private fun handleBackButtonClicked(action: PrivateChatAction.BackButtonClicked) {
        sendEvent { PrivateChatEvent.NavigateBack }
    }

    private fun handleResumedStateChanged(action: PrivateChatAction.OnResumedStateChanged) {
        if (action.isResumed) {
            communicationManager.onChatScreenOpened(chatId = inputParams.chatId)
        } else {
            communicationManager.onChatScreenClosed(chatId = inputParams.chatId)
        }
    }

    private fun handleOnDialogResult(action: PrivateChatAction.OnDialogResult) {
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

    private fun handleOnFirstVisibleItemChanged(action: PrivateChatAction.OnFirstVisibleItemChanged) {
        viewModelScope.launch(dispatcherManager.default) {
            if (action.itemId != null) {
                val isValidMessageId = action.itemId.length == 36
                if (isValidMessageId) {
                    messagePagingDelegate.updateWindowStartMessageId(messageId = action.itemId)
                }
            }
        }
    }

    private fun handleUserClicked(action: PrivateChatAction.UserClicked) {
        sendEvent { PrivateChatEvent.NavigateToProfileScreen(userDeviceAddress = action.user.deviceAddress) }
    }

    private fun handleMessageImageClicked(action: PrivateChatAction.MessageImageClicked) {
        sendEvent {
            PrivateChatEvent.NavigateToViewImageScreen(
                chatId = inputParams.chatId,
                messageId = action.message.id,
            )
        }
    }

    private fun handleMessageActionClicked(action: PrivateChatAction.MessageActionClicked) {
        val loadedState = state.value as? PrivateChatState.Loaded ?: return
        viewModelScope.launch(dispatcherManager.default) {
            val actionResult = messageActionHandlerDelegate.handleAction(message = action.message, action.action)
            when (actionResult) {
                is MessageActionResult.SaveImageAttempted -> {
                    when (actionResult.result) {
                        is SaveImageResult.Success -> {
                            sendEvent { PrivateChatEvent.ShowImageSavedSnackbar }
                        }

                        is SaveImageResult.Error -> {
                            if (actionResult.result is SaveImageResult.Error.NoWriteStoragePermission) {
                                sendEvent { PrivateChatEvent.RequestWriteStoragePermission }
                            }
                        }
                    }
                }

                is MessageActionResult.DisplayReply -> {
                    setState { loadedState.copy(quotedMessage = action.message) }
                    sendEvent { PrivateChatEvent.RequestInputFieldFocus }
                }

                is MessageActionResult.TextCopied -> {
                    sendEvent { PrivateChatEvent.ShowTextCopiedSnackbar }
                }
            }
        }
    }

    private fun handleChatActionClicked(action: PrivateChatAction.ChatActionClicked) {
        viewModelScope.launch(dispatcherManager.default) {
            chatActionHandlerDelegate.handleAction(
                ActionHandlerDelegateAction.ActionClicked(action = action.action)
            )
        }
    }

    private fun handleFooterActionClicked(action: PrivateChatAction.FooterActionClicked) {
        val loadedState = state.value as? PrivateChatState.Loaded ?: return

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
                                sendEvent { PrivateChatEvent.RequestBluetoothPermission }
                            }
                        }

                        BUTTON_ID_RESTART_CHAT -> {
                            communicationManager.inviteUserToPrivateChat(userAddress = inputParams.chatId)
                        }
                    }
                }

                is ChatFooterAction.FileClicked -> {
                    sendEvent { PrivateChatEvent.OpenExternalGalleryForImage }
                }

                is ChatFooterAction.ClearReplyClicked -> {
                    setState { loadedState.copy(quotedMessage = null) }
                }
            }
        }
    }

    private fun handleExternalGalleryContentSelected(action: PrivateChatAction.ExternalGalleryContentSelected) {
        val loadedState = state.value as? PrivateChatState.Loaded ?: return
        viewModelScope.launch {
            val image = imageProcessor.saveChatImage(uri = action.uri, chatId = loadedState.chat.id).getOrNull()

            if (image != null) {
                sendMessage(content = image)
            } else {
                sendEvent { PrivateChatEvent.ShowDialog(InvalidImageDialogInputParams) }
            }
        }
    }

    private fun handleBluetoothPermissionResult(action: PrivateChatAction.OnBluetoothPermissionResult) {
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
        val loadedState = state.value as? PrivateChatState.Loaded ?: return

        if (loadedState.isConnectActionPending) {
            if (communicationManager.isBluetoothEnabled()) {
                btServiceManager.ensureStarted()
                connect()
            } else {
                sendEvent { PrivateChatEvent.RequestEnabledBluetooth }
            }
        }
    }

    private fun handleWriteStoragePermissionResult(action: PrivateChatAction.OnWriteStoragePermissionResult) {
        //TODO: Save image? (needs action caching)
    }

    private fun handleEnableBluetoothResult(action: PrivateChatAction.OnEnableBluetoothResult) {
        val loadedState = state.value as? PrivateChatState.Loaded ?: return

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
        if (scanner.isDevicePaired(address = inputParams.chatId)) {
            val connected = communicationManager.connect(deviceAddress = inputParams.chatId)

            if (!connected) {
                analyticsClient.reportConnectErrorDialogShown()

                sendEvent {
                    PrivateChatEvent.ShowDialog(
                        params = DialogInputParams.TextDialog(
                            title = UiText.Resource(CoreUiR.string.dialog_error),
                            message = UiText.Resource(CoreUiR.string.connection_error_dialog_message),
                        )
                    )
                }
            }
        } else {
            sendEvent {
                PrivateChatEvent.ShowDialog(
                    params = DialogInputParams.TextDialog(
                        title = UiText.Resource(CoreUiR.string.dialog_error),
                        message = UiText.Resource(CoreUiR.string.device_not_paired_anymore_message),
                    )
                )
            }
        }
    }

    private suspend fun sendMessage(content: MessageContent) {
        val loadedState = state.value as? PrivateChatState.Loaded ?: return

        communicationManager.sendPrivateMessage(
            chat = loadedState.chat.toDomain(),
            quotedMessageId = loadedState.quotedMessage?.id,
            content = content
        )

        setState { loadedState.copy(quotedMessage = null) }
        sendEvent { PrivateChatEvent.ScrollToLastMessage }
    }

    private data class PrivateChatDataHolder(
        val chat: Chat.Private?,
        val messages: List<Message>,
        val connectionState: Map<String, ConnectionState>,
        val filesBeingDownloaded: List<FileState.Downloading>,
    )

    private companion object {
        const val BUTTON_ID_CONNECT = 0
        const val BUTTON_ID_RESTART_CHAT = 1

        fun getInputParams(savedStateHandle: SavedStateHandle) =
            requireNotNull(PrivateChatScreenDestination.argsFrom(savedStateHandle))
    }
}
