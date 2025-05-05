package com.example.expense_tracker.utils;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

// Database definition
@Database(entities = { UserGuidEntity.class }, version = 1, exportSchema = false)
public abstract class UserGuidDatabase extends RoomDatabase {
    public abstract UserGuidDao userGuidDao();

    private static volatile UserGuidDatabase INSTANCE;

    public static UserGuidDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (UserGuidDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            UserGuidDatabase.class,
                            "user_guid_database")
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}