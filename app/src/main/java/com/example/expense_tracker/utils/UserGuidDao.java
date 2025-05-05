package com.example.expense_tracker.utils;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;
import io.reactivex.Flowable;
import io.reactivex.Single;

// Data Access Object for user GUIDs
@Dao
public interface UserGuidDao {
    // Original synchronous method
    @Query("SELECT * FROM user_guids WHERE userId = :userId LIMIT 1")
    UserGuidEntity getUserGuid(String userId);

    // Async version using RxJava
    @Query("SELECT * FROM user_guids WHERE userId = :userId LIMIT 1")
    Single<UserGuidEntity> getUserGuidAsync(String userId);

    // Original synchronous method
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertUserGuid(UserGuidEntity userGuid);

    // Async version
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Single<Long> insertUserGuidAsync(UserGuidEntity userGuid);

    // Original synchronous method
    @Query("DELETE FROM user_guids WHERE userId = :userId")
    void deleteUserGuid(String userId);

    // Async version
    @Query("DELETE FROM user_guids WHERE userId = :userId")
    Single<Integer> deleteUserGuidAsync(String userId);

    // Original synchronous method
    @Query("DELETE FROM user_guids")
    void deleteAllUserGuids();

    // Async version
    @Query("DELETE FROM user_guids")
    Single<Integer> deleteAllUserGuidsAsync();

    // Original synchronous method
    @Query("SELECT * FROM user_guids")
    List<UserGuidEntity> getAllUsers();

    // Async version
    @Query("SELECT * FROM user_guids")
    Flowable<List<UserGuidEntity>> getAllUsersAsync();
}