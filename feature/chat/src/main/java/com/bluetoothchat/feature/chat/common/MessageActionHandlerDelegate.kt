package com.bluetoothchat.feature.chat.common

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.core.content.ContextCompat.getSystemService
import com.bluetoothchat.core.dispatcher.DispatcherManager
import com.bluetoothchat.core.filemanager.file.FileState
import com.bluetoothchat.core.ui.model.ViewMessage
import com.bluetoothchat.core.ui.model.ViewMessageAction
import com.bluetoothchat.core.ui.model.ViewMessageContent
import com.bluetoothchat.core.ui.model.primaryContent
import com.bluetoothchat.feature.chat.image.saver.ImageSaver
import com.bluetoothchat.feature.chat.image.saver.SaveImageResult
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MessageActionHandlerDelegate @Inject constructor(
    @ApplicationContext private val context: Context,
    private val imageSaver: ImageSaver,
    private val dispatcherManager: DispatcherManager,
) {

    suspend fun handleAction(message: ViewMessage.Plain, action: ViewMessageAction): MessageActionResult {
        return when (action) {
            ViewMessageAction.REPLY -> {
                MessageActionResult.DisplayReply(message = message)
            }

            ViewMessageAction.COPY -> {
                //This code will crash if run not on the Main thread on pre API 28 device
                withContext(dispatcherManager.main) {
                    val textContent = message.content.primaryContent() as? ViewMessageContent.Text

                    if (textContent != null) {
                        val clipboard = getSystemService(context, ClipboardManager::class.java)
                        val clip = ClipData.newPlainText("label", textContent.text)
                        clipboard?.setPrimaryClip(clip)
                    }

                    MessageActionResult.TextCopied
                }
            }

            ViewMessageAction.SAVE_TO_GALLERY -> {
                val imageContent = message.content.primaryContent() as? ViewMessageContent.Image
                val downloadedImageFile = imageContent?.file as? FileState.Downloaded

                if (downloadedImageFile != null) {
                    val saveResult = imageSaver.save(filePath = downloadedImageFile.path)
                    MessageActionResult.SaveImageAttempted(result = saveResult)
                } else {
                    MessageActionResult.SaveImageAttempted(result = SaveImageResult.Error.SaveError)
                }
            }
        }
    }
}

sealed interface MessageActionResult {
    data class SaveImageAttempted(val result: SaveImageResult) : MessageActionResult
    data class DisplayReply(val message: ViewMessage.Plain) : MessageActionResult
    object TextCopied : MessageActionResult
}
