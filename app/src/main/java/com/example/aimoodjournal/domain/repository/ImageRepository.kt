package com.example.aimoodjournal.domain.repository

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri

/**
 * Repository interface for handling image processing and storage operations.
 * 
 * This repository encapsulates all image-related functionality including:
 * - Image loading and conversion
 * - Image storage to internal storage
 * - Bitmap processing and optimization
 */
interface ImageRepository {
    
    /**
     * Loads a bitmap from a URI with proper error handling.
     * 
     * @param context Android application context
     * @param uri The URI of the image to load
     * @return The loaded bitmap, or null if loading fails
     */
    suspend fun loadBitmapFromUri(context: Context, uri: Uri): Bitmap?
    
    /**
     * Saves an image from a URI to internal storage.
     * 
     * @param context Android application context
     * @param uri The URI of the image to save
     * @return The file path where the image was saved
     * @throws IllegalArgumentException if the URI is invalid
     * @throws SecurityException if the app lacks necessary permissions
     */
    suspend fun saveImageToInternalStorage(context: Context, uri: Uri): String
} 