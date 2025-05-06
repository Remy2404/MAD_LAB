package com.example.expense_tracker.utils;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.webkit.MimeTypeMap;

import com.example.expense_tracker.BuildConfig;
import com.example.expense_tracker.utils.ImageStorageHelper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Java wrapper for Supabase operations - workaround for Kotlin compatibility
 * issues
 */
public class SupabaseHelper {
    private static final String TAG = "SupabaseHelper";
    private static final String BUCKET_NAME = "receipt_images";

    private static boolean initialized = false;

    /**
     * Initialize Supabase. Call this before any other operations.
     */
    public static void initialize() {
        if (!initialized) {
            try {
                // Initialize via the Kotlin SupabaseManager
                SupabaseManager.INSTANCE.initialize();
                initialized = true;
                Log.d(TAG, "Supabase initialized successfully via Kotlin helper");
            } catch (Exception e) {
                Log.e(TAG, "Failed to initialize Supabase: " + e.getMessage(), e);
            }
        }
    }

    /**
     * Check if Supabase is initialized
     */
    public static boolean isInitialized() {
        return initialized;
    }

    /**
     * Upload image to Supabase storage
     * 
     * @param context  Android context
     * @param imageUri URI of the image to upload
     * @return CompletableFuture with the public URL of the image
     */
    public static CompletableFuture<String> uploadImage(Context context, Uri imageUri) {
        if (!initialized) {
            CompletableFuture<String> future = new CompletableFuture<>();
            future.completeExceptionally(new IllegalStateException("Supabase client not initialized"));
            return future;
        }

        try {
            return SupabaseManager.INSTANCE.uploadImage(context, imageUri);
        } catch (Exception e) {
            CompletableFuture<String> future = new CompletableFuture<>();
            future.completeExceptionally(e);
            return future;
        }
    }

    /**
     * Upload image to Supabase storage with fallback to mock storage
     * 
     * @param context  Android context
     * @param imageUri URI of the image to upload
     * @return CompletableFuture with the public URL (real or mock)
     */
    public static CompletableFuture<String> uploadImageWithFallback(Context context, Uri imageUri) {
        CompletableFuture<String> resultFuture = new CompletableFuture<>();
        // Attempt Supabase first
        if (isInitialized()) {
            uploadImage(context, imageUri).thenAccept(url -> {
                resultFuture.complete(url);
            }).exceptionally(ex -> {
                Log.w(TAG, "Supabase upload failed, falling back to mock: " + ex.getMessage());
                // Fallback to mock
                ImageStorageHelper.uploadImage(context, imageUri).thenAccept(mockUrl -> {
                    resultFuture.complete(mockUrl);
                }).exceptionally(mockEx -> {
                    resultFuture.completeExceptionally(mockEx);
                    return null;
                });
                return null;
            });
        } else {
            // Directly use mock if not initialized
            Log.w(TAG, "Supabase not initialized, using mock storage");
            ImageStorageHelper.uploadImage(context, imageUri).thenAccept(mockUrl -> {
                resultFuture.complete(mockUrl);
            }).exceptionally(mockEx -> {
                resultFuture.completeExceptionally(mockEx);
                return null;
            });
        }
        return resultFuture;
    }

    /**
     * Get file extension from URI
     */
    private static String getFileExtension(Context context, Uri uri) {
        if (uri.getScheme() != null && uri.getScheme().equals("content")) {
            String mimeType = context.getContentResolver().getType(uri);
            String extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType);
            if (extension != null) {
                return extension;
            }
        }

        String path = uri.getPath();
        if (path != null) {
            int dot = path.lastIndexOf(".");
            if (dot > 0 && dot < path.length() - 1) {
                return path.substring(dot + 1);
            }
        }

        return "jpg"; // Default extension
    }

    /**
     * Get file bytes from URI
     */
    private static byte[] getFileBytes(Context context, Uri uri) throws IOException {
        InputStream inputStream = null;
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        try {
            inputStream = context.getContentResolver().openInputStream(uri);
            if (inputStream == null) {
                throw new IOException("Cannot open input stream for URI: " + uri);
            }

            byte[] data = new byte[16384]; // 16KB buffer
            int bytesRead;

            while ((bytesRead = inputStream.read(data)) != -1) {
                buffer.write(data, 0, bytesRead);
            }

            buffer.flush();
            return buffer.toByteArray();
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                Log.e(TAG, "Error closing input stream", e);
            }
        }
    }
}
