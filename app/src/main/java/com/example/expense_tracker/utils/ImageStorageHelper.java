package com.example.expense_tracker.utils;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.webkit.MimeTypeMap;

import com.example.expense_tracker.models.Expense;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * A simple mock implementation that simulates image storage
 * This will be used temporarily until Supabase integration works
 */
public class ImageStorageHelper {
    private static final String TAG = "ImageStorageHelper";
    
    /**
     * Upload image to storage and return a mock URL
     * @param context Android context
     * @param imageUri URI of the image to upload
     * @return CompletableFuture with the URL of the image (mock for now)
     */
    public static CompletableFuture<String> uploadImage(Context context, Uri imageUri) {
        CompletableFuture<String> resultFuture = new CompletableFuture<>();
        
        try {
            // Generate a unique file name
            String uuid = UUID.randomUUID().toString();
            String extension = getFileExtension(context, imageUri);
            
            // For now, we'll return a mocked public URL
            // In a real implementation, this would be the URL from Supabase or another storage solution
            String mockUrl = "https://example.com/receipts/" + uuid + "." + extension;
            
            // Simulate some network delay
            new Thread(() -> {
                try {
                    Thread.sleep(1500); // Simulate upload time
                    resultFuture.complete(mockUrl);
                } catch (InterruptedException e) {
                    resultFuture.completeExceptionally(e);
                }
            }).start();
            
            Log.d(TAG, "Mock upload successful, URL: " + mockUrl);
        } catch (Exception e) {
            Log.e(TAG, "Error preparing upload: " + e.getMessage(), e);
            resultFuture.completeExceptionally(e);
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
     * Get file bytes from URI - used for debugging
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
