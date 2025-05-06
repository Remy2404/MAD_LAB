package com.example.expense_tracker.utils;

import android.content.Context;

/**
 * Configuration class for Supabase
 * This class serves as a bridge to the Kotlin-based SupabaseManager
 */
public class SupabaseConfig {
    /**
     * Initialize the Supabase client
     * Call this method in your Application class onCreate
     */
    public static void initialize(Context context) {
        // Use the Kotlin wrapper to initialize Supabase
        SupabaseManager.INSTANCE.initialize();
    }

    /**
     * Check if Supabase is initialized
     */
    public static boolean isInitialized() {
        return SupabaseManager.INSTANCE.isInitialized();
    }
}
