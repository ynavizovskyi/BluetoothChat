package com.bluetoothchat.core.ui.model.mapper

import com.bluetoothchat.core.domain.model.MessageContent
import com.bluetoothchat.core.filemanager.file.FileManager
import com.bluetoothchat.core.filemanager.file.FileState
import com.bluetoothchat.core.ui.model.ViewMessageContent
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ViewMessageContentMapper @Inject constructor(private val fileManager: FileManager) {

    suspend fun map(
        content: MessageContent,
        chatId: String,
        filesBeingDownloaded: List<FileState.Downloading>,
    ): ViewMessageContent = when (content) {
        is MessageContent.Text -> ViewMessageContent.Text(text = content.text)
        is MessageContent.File.Image -> {
            val fileBeingDownloaded =
                filesBeingDownloaded.firstOrNull { it.fileName == content.fileName }

            val file = fileBeingDownloaded ?: fileManager.getChatFile(
                chatId = chatId,
                fileName = content.fileName,
                fileSizeBytes = content.fileSizeBytes,
            )
            ViewMessageContent.Image(
                file = file,
                aspectRatio = content.aspectRatio,
                dominantColor = content.dominantColor
            )
        }
    }

}
