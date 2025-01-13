package com.bluetoothchat.feature.chat.image.saver

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import androidx.core.content.FileProvider
import com.bluetoothchat.core.permission.PermissionManager
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import com.bluetoothchat.core.ui.R as CoreUiR

@Singleton
class ImageSaver @Inject constructor(
    @ApplicationContext private val context: Context,
    private val permissionManager: PermissionManager,
) {

    suspend fun save(filePath: String): SaveImageResult {
        val shareableUri = createShareableUri(filePath = filePath)

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            saveAndroidNew(shareableUri)
        } else {
            if (!permissionManager.writeExternalStoragePermissionGranted()) {
                SaveImageResult.Error.NoWriteStoragePermission
            } else {
                saveAndroidOld(shareableUri)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun saveAndroidNew(inputUri: Uri): SaveImageResult {
        val resolver = context.contentResolver
        val outputUri = externalStorageUri(inputUri)

        var inp: InputStream? = null
        var outp: OutputStream? = null

        return try {
            outp = resolver.openOutputStream(outputUri)
                ?: error("Failed to open output stream")
            inp = resolver.openInputStream(inputUri)
                ?: error("Failed to open input stream")

            inp.copyTo(outp)
            SaveImageResult.Success
        } catch (e: IOException) {
            resolver.delete(outputUri, null, null)
            SaveImageResult.Error.SaveError
        } finally {
            inp?.close()
            outp?.close()
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun externalStorageUri(uri: Uri): Uri {
        val envDir = Environment.DIRECTORY_PICTURES
        val collectionUri = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)

        val appName = context.getString(CoreUiR.string.app_name)
        val relativePath = "$envDir/$appName"

        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, getFileName(appName, uri))
            put(MediaStore.MediaColumns.MIME_TYPE, MIME_TYPE_IMAGE)
            put(MediaStore.MediaColumns.RELATIVE_PATH, relativePath)
        }

        return context.contentResolver.insert(collectionUri, contentValues)
            ?: error("Failed to create new MediaStore record")
    }

    private fun getFileName(appName: String, uri: Uri): String {
        val timeFormat = SimpleDateFormat("yyyy-MM-dd-hh-mm-ss", Locale.US).format(Date())
        return "$appName-${timeFormat}${uri.path?.pathToFileFormat()}"
    }

    private fun saveAndroidOld(uri: Uri): SaveImageResult {
        val file = createFileForSave(uri)
        var inp: InputStream? = null
        var outp: OutputStream? = null

        return try {
            outp = file.outputStream()
            inp = context.contentResolver.openInputStream(uri)
                ?: throw error("Failed to open input stream")

            inp.copyTo(outp)
            SaveImageResult.Success
        } catch (e: IOException) {
            file.delete()
            SaveImageResult.Error.SaveError
        } finally {
            inp?.close()
            outp?.close()
        }
    }

    private fun createFileForSave(uri: Uri): File {
        val envDir = Environment.DIRECTORY_PICTURES
        val saveDirectory = Environment.getExternalStoragePublicDirectory(envDir)
            ?: error("External storage is not initialized")
        val dir = File(saveDirectory, context.getString(CoreUiR.string.app_name))

        if (!dir.exists()) {
            if (!dir.mkdirs())
                throw error("cannot create ${dir.absolutePath}")
        }

        return File(dir, getFileName(context.getString(CoreUiR.string.app_name), uri))
    }

    private fun createShareableUri(filePath: String): Uri {
        val authority = "${context.packageName}.FileProvider"
        return FileProvider.getUriForFile(context, authority, File(filePath))
    }

    private fun String.pathToFileFormat(): String {
        return this.substring(this.lastIndexOf("."))
    }

    companion object {
        const val MIME_TYPE_IMAGE = "image/*"
    }
}
