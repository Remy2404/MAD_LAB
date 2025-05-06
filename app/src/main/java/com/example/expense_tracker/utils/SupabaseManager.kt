package com.example.expense_tracker.utils

import android.content.Context
import android.net.Uri
import android.util.Log
import android.webkit.MimeTypeMap
import com.example.expense_tracker.BuildConfig
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.util.UUID
import java.util.concurrent.CompletableFuture

/**
 * Kotlin wrapper for Supabase operations
 * This class provides a bridge between Java code and Kotlin-based Supabase library
 */
object SupabaseManager {
    private const val TAG = "SupabaseManager"
    private const val BUCKET_NAME = "receipt-images"
    
    private var supabaseClient: SupabaseClient? = null
    
    /**
     * Initialize the Supabase client
     */
    fun initialize() {
        if (supabaseClient == null) {
            try {
                supabaseClient = createSupabaseClient(
                    supabaseUrl = BuildConfig.SUPABASE_URL,
                    supabaseKey = BuildConfig.SUPABASE_ANON_KEY
                ) {
                    install(Storage)
                }
                // Always try to create the bucket; ignore error if it already exists
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        supabaseClient?.storage?.createBucket(BUCKET_NAME)
                        Log.d(TAG, "Created new bucket: $BUCKET_NAME")
                    } catch (e: Exception) {
                        Log.d(TAG, "Bucket may already exist or error occurred: ${e.message}")
                    }
                }
                
                Log.d(TAG, "Supabase initialized successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to initialize Supabase: ${e.message}", e)
            }
        }
    }
    
    /**
     * Check if Supabase client is initialized
     */
    fun isInitialized(): Boolean {
        return supabaseClient != null
    }
    
    /**
     * Upload image to Supabase storage
     * @param context Android context
     * @param imageUri URI of the image to upload
     * @return CompletableFuture with the public URL of the image
     */
    fun uploadImage(context: Context, imageUri: Uri): CompletableFuture<String> {
        val resultFuture = CompletableFuture<String>()
        
        if (supabaseClient == null) {
            resultFuture.completeExceptionally(IllegalStateException("Supabase client not initialized"))
            return resultFuture
        }
        
        try {
            // Generate a unique file name
            val fileName = "${UUID.randomUUID()}.${getFileExtension(context, imageUri)}"
            
            // Read file bytes
            val fileBytes = getFileBytes(context, imageUri)
            // Use Kotlin coroutines for the upload
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    // Just try to upload; assume bucket exists or was created at init
                    supabaseClient?.storage?.from(BUCKET_NAME)?.upload(fileName, fileBytes)
                    // Get the public URL
                    val publicUrl = supabaseClient?.storage?.from(BUCKET_NAME)?.publicUrl(fileName) ?: ""
                    Log.d(TAG, "File uploaded successfully: $publicUrl")
                    resultFuture.complete(publicUrl)
                } catch (e: Exception) {
                    Log.e(TAG, "Upload failed: ${e.message}", e)
                    resultFuture.completeExceptionally(e)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error preparing upload: ${e.message}", e)
            resultFuture.completeExceptionally(e)
        }
        
        return resultFuture
    }
    
    /**
     * Get file extension from URI
     */
    private fun getFileExtension(context: Context, uri: Uri): String {
    return when (uri.scheme) {
        "content" -> {
            val mimeType = context.contentResolver.getType(uri)
            MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType) ?: "jpg"
        }
        "file" -> {
            val path = uri.path
            if (path != null) {
                val idx = path.lastIndexOf('.')
                if (idx >= 0 && idx < path.length - 1) {
                    path.substring(idx + 1)
                } else {
                    "jpg"
                }
            } else {
                "jpg"
            }
        }
        else -> "jpg"
    }
    }
    
    /**
     * Get file bytes from URI
     */
    @Throws(IOException::class)
    private fun getFileBytes(context: Context, uri: Uri): ByteArray {
        val inputStream = context.contentResolver.openInputStream(uri)
            ?: throw IOException("Cannot open input stream for URI: $uri")
            
        return try {
            val buffer = ByteArrayOutputStream()
            val data = ByteArray(16384) // 16KB buffer
            // Read in chunks
            while (true) {
                val bytesRead = inputStream.read(data)
                if (bytesRead <= 0) break
                buffer.write(data, 0, bytesRead)
            }
            buffer.flush()
            buffer.toByteArray()
        } finally {
            try {
                inputStream.close()
            } catch (e: IOException) {
                Log.e(TAG, "Error closing input stream", e)
            }
        }
    }
}

