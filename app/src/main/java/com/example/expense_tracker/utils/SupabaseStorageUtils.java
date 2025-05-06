package com.example.expense_tracker.utils;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import java.util.concurrent.CompletableFuture;

/**
 * Utility class for Supabase Storage operations
 * This class serves as a bridge to the Kotlin-based SupabaseManager
 */
public class SupabaseStorageUtils {
    private static final String TAG = "SupabaseStorageUtils";

    /**
     * Upload an image file to Supabase Storage
     * 
     * @param context  Context for resolving content URIs
     * @param imageUri URI of the image to upload
     * @return CompletableFuture that resolves to the download URL of the uploaded
     *         image
     */
    public static CompletableFuture<String> uploadImage(Context context, Uri imageUri) {
        if (imageUri == null) {
            CompletableFuture<String> future = new CompletableFuture<>();
            future.completeExceptionally(new IllegalArgumentException("Image URI cannot be null"));
            return future;
        }

        if (!SupabaseConfig.isInitialized()) {
            Log.w(TAG, "Supabase not initialized, initializing now");
            SupabaseConfig.initialize(context);
        }

        // Use the Kotlin SupabaseManager to handle the upload
        return SupabaseManager.INSTANCE.uploadImage(context, imageUri);
    }
}
