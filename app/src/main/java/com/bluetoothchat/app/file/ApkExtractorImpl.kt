package com.bluetoothchat.app.file

import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Environment
import androidx.core.content.FileProvider
import com.bluetoothchat.app.BuildConfig
import com.bluetoothchat.core.filemanager.ApkExtractor
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import javax.inject.Inject
import javax.inject.Singleton
import com.bluetoothchat.core.ui.R as CoreUiR

@Singleton
class ApkExtractorImpl @Inject constructor(@ApplicationContext private val context: Context) : ApkExtractor {

    override suspend fun extractApk(): Uri? {
        val application = context.packageManager
            .getPackageInfo(BuildConfig.APPLICATION_ID, PackageManager.GET_SHARED_LIBRARY_FILES)
            ?: return null

        val directory = context.externalCacheDir
            ?: File(Environment.getExternalStorageDirectory(), context.getString(CoreUiR.string.app_name))
        val file = File(application.applicationInfo.publicSourceDir)

        return try {
            val newFile = copyAndZip(file, directory, "BluetoothChat")

            try {
                val authority = "${context.packageName}.FileProvider"
                return FileProvider.getUriForFile(context, authority, newFile)
            } catch (e: IllegalArgumentException) {
                null
            }
        } catch (e: IOException) {
            null
        }
    }

    private fun copyAndZip(file: File, targetDirectory: File, newName: String): File {
        val bufferSize = 2048

        val copiedFile = File(targetDirectory, "$newName.apk")
        val zipFile = File(targetDirectory, "$newName.zip")

        copiedFile.deleteOnExit()
        copiedFile.createNewFile()
        zipFile.deleteOnExit()

        FileInputStream(file).use { fileInputStream ->
            FileOutputStream(copiedFile).use { fileOutputStream ->

                fileOutputStream.channel
                    .transferFrom(fileInputStream.channel, 0, fileInputStream.channel.size())
            }
        }

        FileInputStream(copiedFile).use { fileInputStream ->
            BufferedInputStream(fileInputStream, bufferSize).use { bufferedInputStream ->
                FileOutputStream(zipFile).use { fileOutputStream ->
                    BufferedOutputStream(fileOutputStream).use { bufferedOutputStream ->
                        ZipOutputStream(bufferedOutputStream).use { zipOutputStream ->

                            val data = ByteArray(bufferSize)
                            val entry = ZipEntry(copiedFile.name)
                            zipOutputStream.putNextEntry(entry)

                            var count = bufferedInputStream.read(data, 0, bufferSize)
                            while (count != -1) {
                                zipOutputStream.write(data, 0, count)
                                count = bufferedInputStream.read(data, 0, bufferSize)
                            }
                        }
                    }
                }
            }
        }
        return zipFile
    }
}
