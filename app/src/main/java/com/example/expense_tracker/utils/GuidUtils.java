package com.example.expense_tracker.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.UUID;

public class GuidUtils {
    private static final String TAG = "GuidUtils";
    private static final String PREFS_NAME = "expense_tracker";
    public static final String DB_GUID_PREFIX = "db_guid_";

    /**
     * Get a valid GUID for the current user.
     * If no valid GUID exists, generates a new one and stores it.
     * Each Firebase user gets their own unique database GUID.
     */
    public static String getUserDbGuid(Context context) {
        if (context == null) {
            Log.e(TAG, "Context is null, returning empty GUID");
            return "";
        }
    
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Log.w(TAG, "No user logged in, using empty GUID");
            return "";
        }
    
        String userId = currentUser.getUid(); // Use Firebase UID as the unique identifier
        String guidKey = DB_GUID_PREFIX + userId;
    
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String dbGuid = prefs.getString(guidKey, null);
    
        if (dbGuid == null || !isValidGuid(dbGuid)) {
            // Generate a new GUID if none exists or if the existing one is invalid
            dbGuid = generateGuid();
            Log.d(TAG, "Generated new GUID for user " + userId + ": " + dbGuid);
    
            // Save the GUID for future use
            prefs.edit().putString(guidKey, dbGuid).apply();
        } else {
            Log.d(TAG, "Using existing GUID for user " + userId + ": " + dbGuid);
        }
    
        return dbGuid;
    }

    /**
     * Generates a standard UUID in proper format for API
     */
    public static String generateGuid() {
        return UUID.randomUUID().toString().toLowerCase();
    }

    /**
     * Check if a string is in valid GUID format
     */
    public static boolean isValidGuid(String guid) {
        if (guid == null) return false;

        // Standard UUID format pattern: 8-4-4-4-12 hexadecimal digits
        String pattern = "^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$";
        boolean isValid = guid.toLowerCase().matches(pattern);

        if (!isValid) {
            Log.e(TAG, "Invalid GUID format: " + guid);
        }

        return isValid;
    }

    /**
     * Clear the GUID for the current user.
     * Should be called during logout to ensure data separation.
     */
    public static void clearCurrentUserGuid(Context context) {
        if (context == null) return;

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            String guidKey = DB_GUID_PREFIX + userId;

            SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            prefs.edit().remove(guidKey).apply();
            Log.d(TAG, "Cleared GUID for user: " + userId);
        }
    }

    /**
     * Clear all GUIDs stored in preferences.
     * Use with caution - typically only needed for testing or complete reset.
     */
    public static void clearAllGuids(Context context) {
        if (context == null) return;

        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        // Clear all keys that start with our GUID prefix
        for (String key : prefs.getAll().keySet()) {
            if (key.startsWith(DB_GUID_PREFIX)) {
                editor.remove(key);
            }
        }

        editor.apply();
        Log.d(TAG, "Cleared all user GUIDs");
    }

    /**
     * Save a GUID for the current user.
     * Should be called to store a new GUID for the user.
     */
    public static void saveUserDbGuid(Context context, String guid) {
        if (context == null || guid == null) return;

        // If the provided GUID isn't valid, generate a new one
        if (!isValidGuid(guid)) {
            guid = generateGuid();
            Log.d(TAG, "Provided GUID was invalid, generated new one: " + guid);
        }

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            String guidKey = DB_GUID_PREFIX + userId;

            SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            prefs.edit().putString(guidKey, guid).apply();
            Log.d(TAG, "Saved GUID for user: " + userId);
        }
    }
}