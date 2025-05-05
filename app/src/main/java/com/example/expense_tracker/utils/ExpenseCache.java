package com.example.expense_tracker.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.expense_tracker.models.Expense;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class to cache expenses locally when network is unavailable
 */
public class ExpenseCache {
    private static final String TAG = "ExpenseCache";
    private static final String PREF_NAME = "expense_cache";
    private static final String KEY_EXPENSES = "cached_expenses";
    private static final String KEY_LAST_UPDATE = "last_update_time";

    /**
     * Save expenses to local cache
     * 
     * @param context Application context
     * @param expenses List of expenses to cache
     */
    public static void saveExpenses(Context context, List<Expense> expenses) {
        if (context == null || expenses == null) {
            Log.e(TAG, "Cannot save expenses: context or expenses list is null");
            return;
        }

        try {
            SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            
            Gson gson = new Gson();
            String json = gson.toJson(expenses);
            
            editor.putString(KEY_EXPENSES, json);
            editor.putLong(KEY_LAST_UPDATE, System.currentTimeMillis());
            editor.apply();
            
            Log.d(TAG, "Saved " + expenses.size() + " expenses to local cache");
        } catch (Exception e) {
            Log.e(TAG, "Error saving expenses to cache", e);
        }
    }

    /**
     * Load expenses from local cache
     * 
     * @param context Application context
     * @return List of cached expenses or empty list if none exist
     */
    public static List<Expense> getExpenses(Context context) {
        if (context == null) {
            Log.e(TAG, "Cannot get expenses: context is null");
            return new ArrayList<>();
        }

        try {
            SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            String json = prefs.getString(KEY_EXPENSES, null);
            
            if (json == null) {
                Log.d(TAG, "No cached expenses found");
                return new ArrayList<>();
            }
            
            Gson gson = new Gson();
            Type type = new TypeToken<List<Expense>>(){}.getType();
            List<Expense> expenses = gson.fromJson(json, type);
            
            Log.d(TAG, "Loaded " + expenses.size() + " expenses from local cache");
            return expenses;
        } catch (Exception e) {
            Log.e(TAG, "Error loading expenses from cache", e);
            return new ArrayList<>();
        }
    }

    /**
     * Check if cached data exists and is recent (less than the specified max age)
     * 
     * @param context Application context
     * @param maxAgeMinutes Maximum age of cached data in minutes
     * @return true if cache exists and is not expired
     */
    public static boolean hasFreshCache(Context context, int maxAgeMinutes) {
        if (context == null) {
            return false;
        }

        try {
            SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            long lastUpdate = prefs.getLong(KEY_LAST_UPDATE, 0);
            String json = prefs.getString(KEY_EXPENSES, null);
            
            if (json == null) {
                return false;
            }
            
            // Check if cache is fresh
            long maxAgeMillis = maxAgeMinutes * 60 * 1000L;
            long currentTime = System.currentTimeMillis();
            return (currentTime - lastUpdate < maxAgeMillis);
        } catch (Exception e) {
            Log.e(TAG, "Error checking cache freshness", e);
            return false;
        }
    }

    /**
     * Clear the expense cache
     * 
     * @param context Application context
     */
    public static void clearCache(Context context) {
        if (context == null) {
            return;
        }

        try {
            SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.clear();
            editor.apply();
            Log.d(TAG, "Expense cache cleared");
        } catch (Exception e) {
            Log.e(TAG, "Error clearing expense cache", e);
        }
    }
}