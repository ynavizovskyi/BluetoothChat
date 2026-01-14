package com.bluetoothchat.core.filemanager.image

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Shader
import android.media.ThumbnailUtils
import android.net.Uri
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.drawable.toBitmap
import androidx.palette.graphics.Palette
import coil.ImageLoader
import coil.request.ErrorResult
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.bluetoothchat.core.dispatcher.DispatcherManager
import com.bluetoothchat.core.domain.model.MessageContent
import com.bluetoothchat.core.domain.model.Picture
import com.bluetoothchat.core.filemanager.file.FileManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.Closeable
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.jvm.Throws

@Singleton
class ImageProcessor @Inject constructor(
    @ApplicationContext private val context: Context,
    private val fileManager: FileManager,
    private val dispatcherManager: DispatcherManager,
) {

    /**
     * Retruns file name of a saved file
     */
    suspend fun saveUserImage(uri: Uri): Result<Picture> = withContext(dispatcherManager.io) {
        runCatching {
            val originalBitmap = fetchBitmap(context = context, uri = uri)
            val cropSize = minOf(originalBitmap.width, originalBitmap.height)
            val croppedBitmap = ThumbnailUtils.extractThumbnail(originalBitmap, cropSize, cropSize)

            val scaledBitmap = scaleBitmap(bitmap = croppedBitmap, maxSize = MAX_USER_IMAGE_SIZE)

            val fileName = fileManager.createChatFolderFileName(uri)
            val file = fileManager.createUserFolderImageFile(outputFileName = fileName)

            scaledBitmap.compress(file = file)

            Picture(id = fileName, sizeBytes = file.length())
        }
    }

    suspend fun saveChatImage(uri: Uri, chatId: String): Result<MessageContent.File.Image> =
        withContext(dispatcherManager.io) {
            runCatching {
                val originalBitmap = fetchBitmap(context = context, uri = uri)
                val scaledBitmap = scaleBitmap(bitmap = originalBitmap, maxSize = MAX_CHAT_IMAGE_SIZE)

                val fileName = fileManager.createChatFolderFileName(uri)
                val file = fileManager.createChatFolderImageFile(chatId = chatId, outputFileName = fileName)

                val ratio = scaledBitmap.width.toFloat() / scaledBitmap.height.toFloat()
                val dominantColor = Palette.from(scaledBitmap).generate().lightVibrantSwatch?.rgb ?: Color.Gray.toArgb()
                scaledBitmap.compress(file = file)

                MessageContent.File.Image(
                    fileName = fileName,
                    fileSizeBytes = file.length(),
                    aspectRatio = ratio,
                    dominantColor = dominantColor,
                )
            }
        }

    @Throws(IllegalStateException::class)
    suspend fun fetchBitmap(context: Context, uri: Uri): Bitmap =
        withContext(Dispatchers.IO) {
            val loader = ImageLoader(context)
            val request = ImageRequest.Builder(context)
                .data(uri)
                .allowHardware(false)
                .build()

            when (val requestResult = loader.execute(request)) {
                is SuccessResult -> requestResult.drawable.toBitmap()
                is ErrorResult -> throw requestResult.throwable
            }
        }

    suspend fun createCircleBitmap(bitmap: Bitmap, scale: Float = 1f): Bitmap {
        val size: Int = (scale * Math.min(bitmap.width, bitmap.height)).toInt()
        val icon = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(icon)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
        val center = size * 0.5f
        val radius = (size / 2).toFloat()

        paint.color = android.graphics.Color.BLACK
        val shader = BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
        val shift = Matrix()
        shift.setTranslate(-(bitmap.width - size) / 2.0f, -(bitmap.height - size) / 2.0f)
        shift.setScale(scale, scale)
        shader.setLocalMatrix(shift)
        paint.setShader(shader)
        canvas.drawCircle(center, center, radius, paint)

        canvas.setBitmap(null)
        return icon
    }

    private fun scaleBitmap(bitmap: Bitmap, maxSize: Int): Bitmap {
        val sampledWidth = bitmap.width.toDouble()
        val sampledHeight = bitmap.height.toDouble()
        val scale = minOf(maxSize / sampledWidth, maxSize / sampledHeight)

        return if (scale < 1) {
            val width = (sampledWidth * scale).toInt()
            val height = (sampledHeight * scale).toInt()

            Bitmap.createScaledBitmap(bitmap, width, height, true).also {
                bitmap.recycle()
            }
        } else {
            bitmap
        }
    }

    private fun Bitmap.compress(
        file: File,
        format: Bitmap.CompressFormat = Bitmap.CompressFormat.JPEG,
        quality: Int = DEFAULT_CHAT_IMAGE_QUALITY,
    ) {
        val fileOutputStream = FileOutputStream(file)
        try {
            compress(format, quality, fileOutputStream)
        } finally {
            fileOutputStream.closeQuietly()
        }
    }

    private fun Closeable.closeQuietly() {
        try {
            close()
        } catch (_: Exception) {
        }
    }

}
