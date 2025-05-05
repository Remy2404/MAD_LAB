package com.example.expense_tracker.utils;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

// User GUID Entity class for Room Database
@Entity(tableName = "user_guids")
public class UserGuidEntity {
    @PrimaryKey
    @NonNull
    private String userId;
    private String guid;
    private long createdAt;

    public UserGuidEntity(@NonNull String userId, String guid) {
        this.userId = userId;
        this.guid = guid;
        this.createdAt = System.currentTimeMillis();
    }

    @NonNull
    public String getUserId() {
        return userId;
    }

    public void setUserId(@NonNull String userId) {
        this.userId = userId;
    }

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
}