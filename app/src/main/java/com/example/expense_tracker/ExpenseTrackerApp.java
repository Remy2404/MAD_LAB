package com.example.expense_tracker;

import android.app.Application;

import com.example.expense_tracker.utils.SupabaseConfig;

public class ExpenseTrackerApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        // Initialize Supabase
        SupabaseConfig.initialize(this);
    }
}
