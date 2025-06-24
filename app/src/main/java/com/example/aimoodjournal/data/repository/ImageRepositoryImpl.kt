package com.example.aimoodjournal.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.util.Log
import com.example.aimoodjournal.domain.repository.ImageRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of ImageRepository that handles image processing and storage.
 * 
 * This implementation provides image loading, storage, and format conversion
 * capabilities with proper error handling and optimization.
 */
@Singleton
class ImageRepositoryImpl @Inject constructor() : ImageRepository {

    companion object {
        private const val TAG = "ImageRepositoryImpl"
    }

    override suspend fun loadBitmapFromUri(context: Context, uri: Uri): Bitmap? {
        val original = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val source = ImageDecoder.createSource(context.contentResolver, uri)
            ImageDecoder.decodeBitmap(source)
        } else {
            @Suppress("DEPRECATION")
            android.provider.MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
        }
        // Always convert to ARGB_8888
        return if (original.config != Bitmap.Config.ARGB_8888) {
            original.copy(Bitmap.Config.ARGB_8888, true)
        } else {
            original
        }
    }

    override suspend fun saveImageToInternalStorage(context: Context, uri: Uri): String {
        return withContext(Dispatchers.IO) {
            try {
                val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
                    ?: throw IllegalArgumentException("Cannot open input stream for URI: $uri")
                
                val fileName = "journal_image_${UUID.randomUUID()}.jpg"
                val file = File(context.filesDir, fileName)
                
                inputStream.use { input ->
                    FileOutputStream(file).use { output ->
                        input?.copyTo(output)
                    }
                }
                file.absolutePath
            } catch (e: Exception) {
                Log.e(TAG, "Error saving image to internal storage", e)
                throw e
            }
        }
    }
} 