package com.example.expense_tracker.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.os.AsyncTask;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

public class GuidUtils {
    private static final String TAG = "GuidUtils";
    private static final String PREFS_NAME = "expense_tracker";
    public static final String DB_GUID_PREFIX = "db_guid_";
    private static final CompositeDisposable disposables = new CompositeDisposable();
    private static final ExecutorService executorService = Executors.newSingleThreadExecutor();

    // Flag to control whether to use SharedPreferences (false) or Room Database
    // (true)
    private static final boolean USE_ROOM_DATABASE = true;

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

        if (USE_ROOM_DATABASE) {
            // Use synchronous Room access with proper thread handling
            try {
                return executorService.submit(() -> getUserDbGuidFromRoom(context, userId)).get();
            } catch (Exception e) {
                Log.e(TAG, "Error accessing Room database, falling back to SharedPreferences", e);
                return getUserDbGuidFromPrefs(context, userId);
            }
        } else {
            return getUserDbGuidFromPrefs(context, userId);
        }
    }

    /**
     * Asynchronously get user GUID, with a callback for when it's retrieved
     */
    public interface GuidCallback {
        void onGuidRetrieved(String guid);
    }

    public static void getUserDbGuidAsync(Context context, GuidCallback callback) {
        if (context == null) {
            Log.e(TAG, "Context is null, returning empty GUID");
            if (callback != null)
                callback.onGuidRetrieved("");
            return;
        }

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Log.w(TAG, "No user logged in, using empty GUID");
            if (callback != null)
                callback.onGuidRetrieved("");
            return;
        }

        String userId = currentUser.getUid();

        if (USE_ROOM_DATABASE) {
            getUserDbGuidFromRoomAsync(context, userId, callback);
        } else {
            // SharedPreferences is fast, so we can call it directly
            String guid = getUserDbGuidFromPrefs(context, userId);
            if (callback != null)
                callback.onGuidRetrieved(guid);
        }
    }

    /**
     * Get user GUID from SharedPreferences (original implementation)
     */
    private static String getUserDbGuidFromPrefs(Context context, String userId) {
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
     * Get user GUID from Room Database (synchronous version - DO NOT CALL FROM MAIN
     * THREAD)
     */
    private static String getUserDbGuidFromRoom(Context context, String userId) {
        UserGuidDatabase db = UserGuidDatabase.getDatabase(context);
        UserGuidDao dao = db.userGuidDao();

        try {
            UserGuidEntity userGuid = dao.getUserGuid(userId);

            if (userGuid != null && isValidGuid(userGuid.getGuid())) {
                Log.d(TAG, "Using existing GUID from Room for user " + userId + ": " + userGuid.getGuid());
                return userGuid.getGuid();
            } else {
                // Generate new GUID
                String newGuid = generateGuid();
                Log.d(TAG, "Generated new GUID for user " + userId + ": " + newGuid);

                // Store in Room
                dao.insertUserGuid(new UserGuidEntity(userId, newGuid));

                return newGuid;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error accessing Room database, falling back to SharedPreferences", e);
            return getUserDbGuidFromPrefs(context, userId);
        }
    }

    /**
     * Get user GUID from Room Database asynchronously
     */
    private static void getUserDbGuidFromRoomAsync(Context context, String userId, GuidCallback callback) {
        UserGuidDatabase db = UserGuidDatabase.getDatabase(context);
        UserGuidDao dao = db.userGuidDao();

        // Use RxJava to handle the database operation asynchronously
        Disposable disposable = dao.getUserGuidAsync(userId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        userGuid -> {
                            // Success case
                            if (userGuid != null && isValidGuid(userGuid.getGuid())) {
                                Log.d(TAG,
                                        "Using existing GUID from Room for user " + userId + ": " + userGuid.getGuid());
                                if (callback != null)
                                    callback.onGuidRetrieved(userGuid.getGuid());
                            } else {
                                // Should not happen since Room returns null as empty
                                insertNewGuidAsync(context, dao, userId, callback);
                            }
                        },
                        error -> {
                            // Error case
                            Log.e(TAG, "Error retrieving GUID from Room", error);
                            // Fallback to simple storage method
                            String guid = getUserDbGuidFromPrefs(context, userId);
                            if (callback != null)
                                callback.onGuidRetrieved(guid);
                        });

        // Add to composite disposable to prevent memory leaks
        disposables.add(disposable);
    }

    private static void insertNewGuidAsync(Context context, UserGuidDao dao, String userId, GuidCallback callback) {
        // Generate a new GUID
        String newGuid = generateGuid();
        Log.d(TAG, "Generated new GUID for user " + userId + ": " + newGuid);

        // Create the entity to insert
        UserGuidEntity entity = new UserGuidEntity(userId, newGuid);

        // Insert asynchronously
        Disposable disposable = dao.insertUserGuidAsync(entity)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        id -> {
                            Log.d(TAG, "Successfully inserted new GUID into Room");
                            if (callback != null)
                                callback.onGuidRetrieved(newGuid);
                        },
                        error -> {
                            Log.e(TAG, "Error inserting GUID into Room", error);
                            // Fallback to SharedPreferences
                            String guid = getUserDbGuidFromPrefs(context, userId);
                            if (callback != null)
                                callback.onGuidRetrieved(guid);
                        });

        // Add to composite disposable to prevent memory leaks
        disposables.add(disposable);
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
        if (guid == null)
            return false;

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
        if (context == null)
            return;

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();

            if (USE_ROOM_DATABASE) {
                clearUserGuidFromRoomAsync(context, userId);
            } else {
                String guidKey = DB_GUID_PREFIX + userId;
                SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
                prefs.edit().remove(guidKey).apply();
                Log.d(TAG, "Cleared GUID for user from SharedPreferences: " + userId);
            }
        }
    }

    private static void clearUserGuidFromRoomAsync(Context context, String userId) {
        UserGuidDatabase db = UserGuidDatabase.getDatabase(context);
        UserGuidDao dao = db.userGuidDao();

        executorService.execute(() -> {
            try {
                dao.deleteUserGuid(userId);
                Log.d(TAG, "Cleared GUID for user from Room: " + userId);
            } catch (Exception e) {
                Log.e(TAG, "Error clearing GUID from Room", e);
            }
        });
    }

    /**
     * Clear all GUIDs stored in preferences.
     * Use with caution - typically only needed for testing or complete reset.
     */
    public static void clearAllGuids(Context context) {
        if (context == null)
            return;

        if (USE_ROOM_DATABASE) {
            clearAllGuidsFromRoomAsync(context);
        } else {
            SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();

            // Clear all keys that start with our GUID prefix
            for (String key : prefs.getAll().keySet()) {
                if (key.startsWith(DB_GUID_PREFIX)) {
                    editor.remove(key);
                }
            }

            editor.apply();
            Log.d(TAG, "Cleared all user GUIDs from SharedPreferences");
        }
    }

    private static void clearAllGuidsFromRoomAsync(Context context) {
        UserGuidDatabase db = UserGuidDatabase.getDatabase(context);
        UserGuidDao dao = db.userGuidDao();

        executorService.execute(() -> {
            try {
                dao.deleteAllUserGuids();
                Log.d(TAG, "Cleared all user GUIDs from Room");
            } catch (Exception e) {
                Log.e(TAG, "Error clearing all GUIDs from Room", e);
            }
        });
    }

    /**
     * Save a GUID for the current user.
     * Should be called to store a new GUID for the user.
     */
    public static void saveUserDbGuid(Context context, String guid) {
        if (context == null || guid == null)
            return;

        // If the provided GUID isn't valid, generate a new one
        if (!isValidGuid(guid)) {
            guid = generateGuid();
            Log.d(TAG, "Provided GUID was invalid, generated new one: " + guid);
        }

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            final String finalGuid = guid;

            if (USE_ROOM_DATABASE) {
                saveUserGuidToRoomAsync(context, userId, finalGuid);
            } else {
                String guidKey = DB_GUID_PREFIX + userId;
                SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
                prefs.edit().putString(guidKey, finalGuid).apply();
                Log.d(TAG, "Saved GUID for user in SharedPreferences: " + userId);
            }
        }
    }

    private static void saveUserGuidToRoomAsync(Context context, String userId, String guid) {
        UserGuidDatabase db = UserGuidDatabase.getDatabase(context);
        UserGuidDao dao = db.userGuidDao();

        executorService.execute(() -> {
            try {
                dao.insertUserGuid(new UserGuidEntity(userId, guid));
                Log.d(TAG, "Saved GUID for user in Room: " + userId);
            } catch (Exception e) {
                Log.e(TAG, "Error saving GUID to Room", e);
            }
        });
    }

    // Clean up any disposables and executor to prevent memory leaks
    public static void dispose() {
        if (!disposables.isDisposed()) {
            disposables.dispose();
        }
        if (!executorService.isShutdown()) {
            executorService.shutdown();
            try {
                if (!executorService.awaitTermination(800, TimeUnit.MILLISECONDS)) {
                    executorService.shutdownNow();
                }
            } catch (InterruptedException e) {
                executorService.shutdownNow();
            }
        }
    }
}