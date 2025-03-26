package com.example.expense_tracker.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.UUID;

public class GuidUtils {
    private static final String TAG = "GuidUtils";
    private static final String PREFS_NAME = "expense_tracker";
    private static final String KEY_USER_DB_GUID = "user_db_guid";

    /**
     * Get a valid GUID for the current user.
     * If no valid GUID exists, generates a new one and stores it.
     */
    public static String getUserDbGuid(Context context) {
        if (context == null) {
            Log.e(TAG, "Context is null, returning empty GUID");
            return UUID.randomUUID().toString();
        }

        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String dbGuid = prefs.getString(KEY_USER_DB_GUID, null);
        
        if (dbGuid == null || !isValidGuid(dbGuid)) {
            // Generate a new GUID in proper format with hyphens
            dbGuid = generateGuid();
            Log.d(TAG, "Generated new GUID: " + dbGuid);
            
            // Save it for future use
            prefs.edit().putString(KEY_USER_DB_GUID, dbGuid).apply();
        } else {
            Log.d(TAG, "Using existing GUID: " + dbGuid);
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
     * Reset the stored GUID, forcing a new one to be generated next time
     */
    public static void resetGuid(Context context) {
        if (context == null) return;
        
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().remove(KEY_USER_DB_GUID).apply();
        Log.d(TAG, "GUID reset, will generate new one on next call");
    }
}