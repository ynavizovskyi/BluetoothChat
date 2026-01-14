package com.bluetoothchat.core.filemanager.file

import android.content.Context
import android.net.Uri
import com.bluetoothchat.core.dispatcher.DispatcherManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FileManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val dispatcherManager: DispatcherManager,
) {

    suspend fun getChatAvatarPictureFile(fileName: String, sizeBytes: Long): FileState =
        withContext(dispatcherManager.io) {
            getFile(folder = getOrCreateUsersFolder(), fileName = fileName, sizeBytes = sizeBytes)
        }

    suspend fun getChatFile(chatId: String, fileName: String, fileSizeBytes: Long): FileState =
        withContext(dispatcherManager.io) {
            getFile(folder = getOrCreateChatFolder(chatId = chatId), fileName = fileName, sizeBytes = fileSizeBytes)
        }

    private suspend fun getFile(folder: File, fileName: String, sizeBytes: Long): FileState =
        withContext(dispatcherManager.io) {
            val file = File(folder, fileName)
            val fullPath = file.path
            if (file.exists() && (sizeBytes == 0L || file.length() == sizeBytes)) {
                FileState.Downloaded(fileName = fileName, path = fullPath, fileSizeBytes = file.length())
            } else {
                FileState.Missing(fileName = fileName, fileSizeBytes = 0)
            }
        }

    suspend fun createChatFolderImageFile(chatId: String, outputFileName: String): File {
        val chatFolder = getOrCreateChatFolder(chatId)
        return File(chatFolder, outputFileName)
    }

    suspend fun createUserFolderImageFile(outputFileName: String): File {
        val chatFolder = getOrCreateUsersFolder()
        return File(chatFolder, outputFileName)
    }

    suspend fun getOrCreateChatFolder(chatId: String): File {
        val chatsFolder = getOrCreateFolder(CHATS_FOLDER)
        val specificChatFolder = File(chatsFolder, chatId)
        if (!specificChatFolder.exists()) {
            specificChatFolder.mkdir()
        }
        return specificChatFolder
    }

    suspend fun getOrCreateUsersFolder() = getOrCreateFolder(USERS_FOLDER)

    suspend fun deleteChatFolder(chatId: String) {
        deleteRecursive(fileOrDir = getOrCreateChatFolder(chatId = chatId))
    }

    private fun getOrCreateFolder(folder: String): File {
        val file = File(getOutputDirectory(), folder)
        if (!file.exists()) {
            file.mkdir()
        }
        return file
    }

    private fun getOutputDirectory(): File? {
        return context.getExternalFilesDir(null)
    }

    fun createChatFolderFileName(uri: Uri): String {
        //TODO:
//        val fileFormat = uri.path?.pathToFileFormat()!!
        val fileName = UUID.randomUUID().toString()
//        return "$fileName$fileFormat"
        return "${fileName}.jpg"
    }

    private fun deleteRecursive(fileOrDir: File) {
        if (fileOrDir.isDirectory) {
            fileOrDir.listFiles()?.forEach { child ->
                deleteRecursive(child)
            }
        }
        fileOrDir.delete()
    }

}
