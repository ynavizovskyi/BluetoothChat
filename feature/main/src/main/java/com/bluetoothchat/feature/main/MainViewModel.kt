package com.bluetoothchat.feature.main

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.bluetoothchat.core.bluetooth.BtServiceManager
import com.bluetoothchat.core.bluetooth.message.manager.CommunicationManagerImpl
import com.bluetoothchat.core.config.RemoteConfig
import com.bluetoothchat.core.db.datasource.ChatDataSource
import com.bluetoothchat.core.db.datasource.MessageDataSource
import com.bluetoothchat.core.db.datasource.UserDataSource
import com.bluetoothchat.core.dispatcher.DispatcherManager
import com.bluetoothchat.core.domain.model.Chat
import com.bluetoothchat.core.domain.model.Message
import com.bluetoothchat.core.domain.model.User
import com.bluetoothchat.core.permission.BluetoothPermissionType
import com.bluetoothchat.core.permission.PermissionManager
import com.bluetoothchat.core.permission.PermissionStatus
import com.bluetoothchat.core.permission.getBluetoothPermissions
import com.bluetoothchat.core.permission.isGranted
import com.bluetoothchat.core.prefs.session.SessionPrefs
import com.bluetoothchat.core.session.NetworkChecker
import com.bluetoothchat.core.session.Session
import com.bluetoothchat.core.ui.components.dialog.model.DialogButton
import com.bluetoothchat.core.ui.components.dialog.model.DialogInputParams
import com.bluetoothchat.core.ui.components.dialog.model.DialogOption
import com.bluetoothchat.core.ui.delegate.ChatActionHandlerDelegate
import com.bluetoothchat.core.ui.delegate.ChatActionHandlerDelegateEvent
import com.bluetoothchat.core.ui.model.UiText
import com.bluetoothchat.core.ui.model.ViewChat
import com.bluetoothchat.core.ui.model.ViewChatWithMessages
import com.bluetoothchat.core.ui.model.ViewUser
import com.bluetoothchat.core.ui.model.mapper.ViewMessageMapper
import com.bluetoothchat.core.ui.model.mapper.ViewUserMapper
import com.bluetoothchat.core.ui.model.mapper.chat.ViewChatMapper
import com.bluetoothchat.core.ui.model.toDomain
import com.bluetoothchat.core.ui.mvi.MviViewModel
import com.bluetoothchat.core.ui.mvi.action.handler.ActionHandlerDelegateAction
import com.bluetoothchat.core.ui.util.TimeFormatType
import com.bluetoothchat.core.ui.util.combine
import com.bluetoothchat.feature.main.contract.MainAction
import com.bluetoothchat.feature.main.contract.MainEvent
import com.bluetoothchat.feature.main.contract.MainState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.bluetoothchat.core.ui.R as CoreUiR

@HiltViewModel
internal class MainViewModel @Inject constructor(
    private val btServiceManager: BtServiceManager,
    private val communicationManager: CommunicationManagerImpl,
    private val session: Session,
    private val sessionPrefs: SessionPrefs,
    private val dispatcherManager: DispatcherManager,
    private val chatDataSource: ChatDataSource,
    private val messageDataSource: MessageDataSource,
    private val userDataSource: UserDataSource,
    private val userMapper: ViewUserMapper,
    private val chatMapper: ViewChatMapper,
    private val messageMapper: ViewMessageMapper,
    private val chatActionHandlerDelegate: ChatActionHandlerDelegate,
    private val config: RemoteConfig,
    private val networkChecker: NetworkChecker,
    private val analyticsClient: MainAnalyticsClient,
    private val permissionManager: PermissionManager,
) : MviViewModel<MainState, MainAction, MainEvent>(MainState.Loading) {

    init {
        viewModelScope.launch(dispatcherManager.default) {
            analyticsClient.reportScreenShown(chatCount = chatDataSource.getTotalChatCount())
        }

        observeData()
        observeChatActionDelegateEvent()
        checkForStartupActions()
    }

    override fun handleAction(action: MainAction) {
        when (action) {
            is MainAction.CreateNewButtonClicked -> handleCreateNewButtonClicked()
            is MainAction.ProfileClicked -> handleProfileClicked()
            is MainAction.SettingsClicked -> handleSettingsClicked()
            is MainAction.OpenSystemAppSettingsClicked -> handleAppSystemSettingsClicked()
            is MainAction.OnBluetoothPermissionsStatusChanged -> handleBluetoothPermissionStatusChanged(action)
            is MainAction.OnNotificationPermissionStatusChanged -> handleNotificationPermissionStatusChanged(action)
            is MainAction.OnDialogResult -> handleOnDialogResult(action)
            is MainAction.ChatClicked -> handleChatClicked(action)
            is MainAction.ChatActionClicked -> handleChatActionClicked(action)
            is MainAction.OnLaunchInAppReviewResult -> handleOnLaunchInAppReviewResult(action)
        }
    }

    //Should be called after permissions?
    private fun checkForStartupActions() {
        val millisSinceLastRateAppShown = System.currentTimeMillis() - sessionPrefs.getRateAppLastShownTimestamp()
        viewModelScope.launch(dispatcherManager.default) {
            if (
                !sessionPrefs.getRateAppNeverShowAgain()
                && millisSinceLastRateAppShown > RATE_APP_ASK_AGAIN_DELAY_MILLIS
                && chatDataSource.getTotalChatCount() >= RATE_APP_MIN_CHAT_COUNT
                && networkChecker.isConnected()
            ) {
                analyticsClient.reportRateAppDialogShown()

                sessionPrefs.setRateAppLastShownTimestamp(System.currentTimeMillis())
                sendEvent {
                    MainEvent.ShowDialog(
                        DialogInputParams.TextDialog(
                            id = RATE_APP_DIALOG_ID,
                            title = UiText.Resource(CoreUiR.string.rate_app_dialog_title),
                            message = UiText.Resource(CoreUiR.string.rate_app_dialog_message),
                            confirmButton = DialogButton(
                                id = RATE_APP_DIALOG_RATE_BUTTON_ID,
                                text = UiText.Resource(CoreUiR.string.rate_app_dialog_rate_now),
                            ),
                            cancelButton = DialogButton(
                                id = RATE_APP_DIALOG_NEVER_ASK_BUTTON_ID,
                                text = UiText.Resource(CoreUiR.string.rate_app_dialog_never_ask_again),
                            ),
                        )
                    )
                }
            }
        }
    }

    private fun observeChatActionDelegateEvent() {
        chatActionHandlerDelegate.oneTimeEvent
            .onEach { event ->
                when (event) {
                    is ChatActionHandlerDelegateEvent.CloseChatScreen -> Unit
                    is ChatActionHandlerDelegateEvent.ShowDialog -> {
                        sendEvent { MainEvent.ShowDialog(params = event.params) }
                    }
                }
            }
            .flowOn(dispatcherManager.default)
            .launchIn(viewModelScope)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeData() {
        val lastMessageFlows = chatDataSource.observeAll().flatMapLatest { chats ->
            combine(chats.map { chat ->
                messageDataSource.observeLast(chat.id).map { chat.id to it }
            }) {
                it.toMap()
            }
        }
        val unreadCountFlows = chatDataSource.observeAll().flatMapLatest { chats ->
            combine(chats.map { chat ->
                messageDataSource.observeUnreadCount(chat.id).map { chat.id to it }
            }) {
                it.toMap()
            }
        }

        val currentViewUserFlow = session.observeUser()
            .map { userMapper.map(user = it, connectedDevices = emptyList()) }

        val viewChatsFlow = combine(
            chatDataSource.observeAll(),
            userDataSource.observeAll(),
            currentViewUserFlow,
            lastMessageFlows.onStart { emit(emptyMap()) },
            unreadCountFlows.onStart { emit(emptyMap()) },
            communicationManager.connectedDevicesFlow.onStart { emit(emptyList()) },
            communicationManager.observeUserFilesBeingDownloaded().onStart { emit(emptyList()) },
        ) { chats, allUsers, currentViewUser, lastMessages, unreadCount, connectedDevices, _ ->
            MainDataHolder(
                chats = chats,
                allUsers = allUsers,
                currentViewUser = currentViewUser,
                lastMessages = lastMessages,
                unreadCount = unreadCount,
                connectedDevices = connectedDevices,
            )
        }
            .conflate()
            .map { holder ->
                val chats = holder.chats
                val currentViewUser = holder.currentViewUser
                val lastMessages = holder.lastMessages
                val unreadCount = holder.unreadCount
                val connectedDevices = holder.connectedDevices
                val allViewUsers = holder.allUsers.map { userMapper.map(it, connectedDevices = connectedDevices) }

                chats.map { chat ->
                    val viewChat = chatMapper.map(chat = chat, connectedDevices = connectedDevices)

                    val viewUsers = when (viewChat) {
                        is ViewChat.Private -> listOf(viewChat.user, currentViewUser)
                        is ViewChat.Group -> allViewUsers
                    }

                    val lastViewMessage = lastMessages[chat.id]?.let { message ->
                        messageMapper.map(
                            chatId = chat.id,
                            message = message,
                            //irrelevant here, we don't need file downloading progress
                            filesBeingDownloaded = emptyList(),
                            currentUser = currentViewUser,
                            timeFormatType = TimeFormatType.TIME_IF_TODAY_DATE_OTHERWISE,
                            displayUserInfo = false,
                            allUsers = viewUsers,
                        )
                    }

                    ViewChatWithMessages(
                        chat = viewChat,
                        lastMessage = lastViewMessage,
                        numOfUnreadMessages = unreadCount[chat.id] ?: 0,
                    )
                }
                    .sortedByDescending { it.lastMessage?.timestamp ?: it.chat.createdTimestamp }
            }

        combine(
            currentViewUserFlow,
            viewChatsFlow,
        ) { currentViewUser, viewChats ->
            setState {
                MainState.Loaded(
                    user = currentViewUser,
                    chats = viewChats,
                )
            }
        }
            .flowOn(dispatcherManager.default)
            .launchIn(viewModelScope)
    }

    private fun handleCreateNewButtonClicked() {
        sendEvent { MainEvent.NavigateToConnectScreen }
    }

    private fun handleProfileClicked() {
        sendEvent { MainEvent.NavigateToCurrentUserProfileScreen }
    }

    private fun handleSettingsClicked() {
        sendEvent { MainEvent.NavigateToSettings }
    }

    private fun handleAppSystemSettingsClicked() {
        sendEvent { MainEvent.OpenSystemAppSettings }
    }

    private fun handleBluetoothPermissionStatusChanged(action: MainAction.OnBluetoothPermissionsStatusChanged) {
        Log.v("MainViewModel", "Bluetooth permission: ${action.toString()}")

        viewModelScope.launch(dispatcherManager.default) {
            when (action.status) {
                is PermissionStatus.Granted -> {
                    if (!action.status.isInitial) {
                        analyticsClient.onBluetoothPermissionsGranted()
                    }

                    btServiceManager.ensureStarted()
                }

                is PermissionStatus.Denied -> {
                    if (!action.status.isInitial) {
                        analyticsClient.onBluetoothPermissionsDenied()
                    }

                    if (action.status.shouldShowRationale) {
                        sendEvent {
                            MainEvent.ShowGrantBluetoothPermissionsSnackbar(
                                permssionType = action.permissions.permissionType,
                            )
                        }
                    }
                }
            }
        }
    }

    private fun handleNotificationPermissionStatusChanged(action: MainAction.OnNotificationPermissionStatusChanged) {
        Log.v("MainViewModel", "Notification permission: ${action.toString()}")

        viewModelScope.launch(dispatcherManager.default) {
            when (action.status) {
                is PermissionStatus.Granted -> {
                    if (!action.status.isInitial) {
                        analyticsClient.onNotificationPermissionGranted()
                    }
                }

                is PermissionStatus.Denied -> {
                    if (!action.status.isInitial) {
                        analyticsClient.onNotificationPermissionDenied()
                    }

                    if (action.status.isInitial) {
                        Log.v("MainViewModel", "Notification permission send event")

                        sendEvent { MainEvent.RequestNotificationPermission }
                    } else if (!action.status.shouldShowRationale) {
                        sendEvent { MainEvent.ShowGrantNotificationPermissionsSnackbar }
                    }
                }
            }

            if ((!action.status.isInitial || action.status.isGranted()) && !permissionManager.bluetoothPermissionsGranted()) {
                if (getBluetoothPermissions().permissionType == BluetoothPermissionType.LOCATION) {
                    sendEvent {
                        MainEvent.ShowDialog(
                            DialogInputParams.TextDialog(
                                id = LOCATION_ACCESS_DIALOG_ID,
                                title = UiText.Resource(CoreUiR.string.location_dialog_title),
                                message = UiText.Resource(CoreUiR.string.location_dialog_message),
                            )
                        )
                    }
                } else {
                    Log.v("MainViewModel", "Bluetooth permission send event")

                    sendEvent { MainEvent.RequestBluetoothPermissions }
                }
            }
        }
    }

    private fun handleOnDialogResult(action: MainAction.OnDialogResult) {
        viewModelScope.launch(dispatcherManager.default) {
            val dialogId = action.result.dialogId
            when {
                chatActionHandlerDelegate.shouldHandleDialogResult(dialogId) -> chatActionHandlerDelegate.handleAction(
                    ActionHandlerDelegateAction.OnDialogResult(result = action.result)
                )

                dialogId == LOCATION_ACCESS_DIALOG_ID -> {
                    sendEvent { MainEvent.RequestBluetoothPermissions }
                }

                dialogId == RATE_APP_DIALOG_ID -> {
                    when ((action.result.option as? DialogOption.ActionButton)?.id) {
                        null -> analyticsClient.reportRateAppNoneOptionSelected()
                        RATE_APP_DIALOG_RATE_BUTTON_ID -> {
                            analyticsClient.reportRateAppRateOptionSelected()

                            sessionPrefs.setRateAppNeverShowAgain()
                            sendEvent { MainEvent.LaunchInAppReview }
                        }

                        RATE_APP_DIALOG_NEVER_ASK_BUTTON_ID -> {
                            analyticsClient.reportRateAppNeverAskOptionSelected()

                            sessionPrefs.setRateAppNeverShowAgain()
                        }
                    }
                }
            }
        }
    }

    private fun handleChatClicked(action: MainAction.ChatClicked) {
        val chat = action.chat.toDomain()
        when (chat) {
            is Chat.Private -> {
                sendEvent {
                    MainEvent.NavigateToPrivateChatScreen(chat.id)
                }
            }

            is Chat.Group -> {
                sendEvent {
                    MainEvent.NavigateToGroupChatScreen(chat.id)
                }
            }
        }
    }

    private fun handleChatActionClicked(action: MainAction.ChatActionClicked) {
        viewModelScope.launch(dispatcherManager.default) {
            chatActionHandlerDelegate.handleAction(ActionHandlerDelegateAction.ActionClicked(action = action.action))
        }
    }

    private fun handleOnLaunchInAppReviewResult(action: MainAction.OnLaunchInAppReviewResult) {
        viewModelScope.launch(dispatcherManager.default) {
            if (action.result) {
                analyticsClient.reportOnLaunchInAppReviewSuccess()
            } else {
                analyticsClient.reportOnLaunchInAppReviewError()
            }
        }
    }


    private data class MainDataHolder(
        val chats: List<Chat>,
        val allUsers: List<User>,
        val currentViewUser: ViewUser,
        val lastMessages: Map<String, Message?>,
        val unreadCount: Map<String, Int>,
        val connectedDevices: List<String>,
    )

    companion object {
        private const val LOCATION_ACCESS_DIALOG_ID = 1
        private const val RATE_APP_DIALOG_ID = 2
        private const val RATE_APP_DIALOG_RATE_BUTTON_ID = 1
        private const val RATE_APP_DIALOG_NEVER_ASK_BUTTON_ID = 2

        private const val RATE_APP_MIN_CHAT_COUNT = 1
        private const val RATE_APP_ASK_AGAIN_DELAY_MILLIS = 3 * 24 * 60 * 60 * 1000
    }
}
