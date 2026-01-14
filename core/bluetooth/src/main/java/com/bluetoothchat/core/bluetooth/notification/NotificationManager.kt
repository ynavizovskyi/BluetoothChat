package com.bluetoothchat.core.bluetooth.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import androidx.core.app.NotificationCompat
import androidx.core.app.Person
import androidx.core.content.FileProvider
import androidx.core.graphics.drawable.IconCompat
import com.bluetoothchat.core.bluetooth.ACTION_STOP
import com.bluetoothchat.core.bluetooth.BtService
import com.bluetoothchat.core.bluetooth.R
import com.bluetoothchat.core.bluetooth.connection.BtConnectionManager
import com.bluetoothchat.core.dispatcher.ApplicationScope
import com.bluetoothchat.core.dispatcher.DispatcherManager
import com.bluetoothchat.core.domain.model.Chat
import com.bluetoothchat.core.domain.model.Message
import com.bluetoothchat.core.domain.model.User
import com.bluetoothchat.core.domain.model.primary
import com.bluetoothchat.core.domain.model.toShortDescription
import com.bluetoothchat.core.filemanager.file.FileManager
import com.bluetoothchat.core.filemanager.file.FileState
import com.bluetoothchat.core.filemanager.image.ImageProcessor
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationManagerWrapper @Inject constructor(
    @ApplicationContext private val context: Context,
    private val intentCreator: NotificationIntentCreator,
    private val stringProvider: NotificationStringProvider,
    private val fileManager: FileManager,
    private val imageProcessor: ImageProcessor,
    private val connectionManager: BtConnectionManager,
    private val dispatcherManager: DispatcherManager,
    private val applicationScope: ApplicationScope,
    private val activityKiller: ActivityKiller,
) {

    private val notificationManager = context.getNotificationManager()

    init {
        connectionManager.connectedDeviceNamesFlow
            .onEach { connectedDeviceNames ->
                if (!activityKiller.isKilled()) {
                    findActiveNotification(FOREGROUND_SERVICE_NOTIFICATION_ID)?.let {
                        val connectedDevicesString = if (connectedDeviceNames.isNotEmpty()) {
                            val devicesString = connectedDeviceNames.joinToString(", ")
                            stringProvider.getConnectedDevicesMessage(devices = devicesString)
                        } else {
                            stringProvider.getNoConnectedDevicesMessage()
                        }
                        val updatedNotification = getForegroundNotification(contentText = connectedDevicesString)
                        notificationManager.notify(FOREGROUND_SERVICE_NOTIFICATION_ID, updatedNotification)
                    }
                }
            }
            .flowOn(dispatcherManager.io)
            .launchIn(applicationScope)
    }

    fun hideChatNotifications(chatId: String) {
        notificationManager.cancel(chatId.chatIdToNotificationId())
    }

    fun getForegroundNotification(contentText: String = stringProvider.getNoConnectedDevicesMessage()): Notification {
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_FOREGROUND,
            stringProvider.getOngoingChannelName(),
            NotificationManager.IMPORTANCE_LOW,
        )
        context.getNotificationManager().createNotificationChannel(channel)

        val builder = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_FOREGROUND)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(stringProvider.getOngoingNotificationTitle())
            .setContentText(contentText)
            .setOngoing(true)
            .setContentIntent(intentCreator.createDeeplinkIntent(ChatAppDeeplink.Connect))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .addAction(0, stringProvider.getStop(), createStopServiceIntent())

        return builder.build()
    }

    suspend fun showChatMessageNotification(message: Message.Plain, chat: Chat, user: User?) {
        val channel = createMessageNotificationChannel()
        val notificationId = chat.notificationId()
        val activeStyle = getActiveNotificationStyle(notificationId = notificationId)

        val messageText = message.content.primary().toShortDescription()
        val messagePerson = createNotificationPerson(user = user)

        val style = NotificationCompat.MessagingStyle(messagePerson)
        if (chat is Chat.Group) {
            style.setConversationTitle(chat.name)
        }
        activeStyle?.let { activeStyle.messages.forEach { style.addMessage(it) } }
        style.addMessage(messageText, message.timestamp, messagePerson)

        val builder = NotificationCompat.Builder(context, channel.id)
            .setStyle(style)
            .setContentTitle(chat.name)
            .setLights(Color.BLUE, 3000, 3000)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(createOpenChatIntent(chat))
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)

        val notification = builder.build()

        notificationManager.notify(notificationId, notification)
    }

    fun showPrivateChatStartedNotification(chat: Chat.Private) {
        val channel = createMessageNotificationChannel()
        val builder = NotificationCompat.Builder(context, channel.id)
            .setContentTitle(chat.name)
            .setContentText(stringProvider.getPrivateChatStarted(name = chat.name))
            .setLights(Color.BLUE, 3000, 3000)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(createOpenChatIntent(chat))
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)

        val notification = builder.build()

        notificationManager.notify(chat.notificationId(), notification)
    }

    fun showAddedToGroupChatNotification(chat: Chat.Group) {
        val channel = createMessageNotificationChannel()
        val builder = NotificationCompat.Builder(context, channel.id)
            .setContentTitle(chat.name)
            .setContentText(stringProvider.getAddedToGroupChat(name = chat.name))
            .setLights(Color.BLUE, 3000, 3000)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(createOpenChatIntent(chat))
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)

        val notification = builder.build()

        notificationManager.notify(chat.notificationId(), notification)
    }

    private fun Context.getNotificationManager() =
        this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    private fun createOpenChatIntent(chat: Chat): PendingIntent {
        val deeplink = when (chat) {
            is Chat.Private -> ChatAppDeeplink.PrivateChat(chatId = chat.id)
            is Chat.Group -> ChatAppDeeplink.GroupChat(chatId = chat.id)
        }
        return intentCreator.createDeeplinkIntent(deeplink)
    }

    private fun createMessageNotificationChannel(): NotificationChannel {
        return NotificationChannel(
            NOTIFICATION_CHANNEL_MESSAGE,
            stringProvider.getMessageChannelName(),
            NotificationManager.IMPORTANCE_HIGH,
        ).apply {
            setShowBadge(true)
            notificationManager.createNotificationChannel(this)
        }
    }

    private fun Chat.notificationId() = id.chatIdToNotificationId()

    private fun String.chatIdToNotificationId() = this.hashCode()

    private suspend fun createNotificationPerson(user: User?): Person {
        val downloadedUserPictureFile = user?.picture?.let {
            fileManager.getChatAvatarPictureFile(fileName = it.id, sizeBytes = it.sizeBytes) as? FileState.Downloaded
        }
        val userIcon = downloadedUserPictureFile?.let {
            runCatching {
                imageProcessor.fetchBitmap(
                    context = context,
                    uri = createExportableUri(downloadedUserPictureFile.path),
                )
            }.getOrNull()?.let { bitmap ->
                val roundBitmap = imageProcessor.createCircleBitmap(
                    bitmap = bitmap,
                    scale = NOTIFICATION_ICON_SCALE_FACTOR,
                )
                IconCompat.createWithBitmap(roundBitmap)
            }
        }
        val userBuilder = Person.Builder()
            .setName(user?.userName ?: "")
        userIcon?.let { userBuilder.setIcon(it) }

        return userBuilder.build()
    }

    private fun getActiveNotificationStyle(notificationId: Int): NotificationCompat.MessagingStyle? {
        return findActiveNotification(notificationId)?.let { notification ->
            NotificationCompat.MessagingStyle.extractMessagingStyleFromNotification(notification)
        }
    }

    private fun findActiveNotification(notificationId: Int) =
        notificationManager.activeNotifications.find { it.id == notificationId }?.notification

    private fun createExportableUri(path: String): Uri {
        val authority = "${context.packageName}.FileProvider"
        return FileProvider.getUriForFile(context, authority, File(path))
    }

    private fun createStopServiceIntent(): PendingIntent {
        val intent = Intent(context, BtService::class.java).apply {
            action = ACTION_STOP
        }
        return PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)
    }

    companion object {
        private const val NOTIFICATION_CHANNEL_MESSAGE = "channel.message"
        private const val NOTIFICATION_CHANNEL_FOREGROUND = "channel.foreground"

        private const val NOTIFICATION_ICON_SCALE_FACTOR = 0.25f
    }
}
