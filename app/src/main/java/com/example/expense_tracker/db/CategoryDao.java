package com.example.expense_tracker.db;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;
import io.reactivex.Flowable;
import io.reactivex.Single;

@Dao
public interface CategoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(CategoryEntity category);

    // Async version using RxJava
    @Query("SELECT * FROM categories")
    Flowable<List<CategoryEntity>> getAllCategoriesAsync();

    // Original synchronous method (keep for compatibility)
    @Query("SELECT * FROM categories")
    List<CategoryEntity> getAllCategories();

    @Query("SELECT * FROM categories WHERE id = :categoryId")
    CategoryEntity getCategoryById(String categoryId);

    // Async version of getCategoryById
    @Query("SELECT * FROM categories WHERE id = :categoryId")
    Single<CategoryEntity> getCategoryByIdAsync(String categoryId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertCategory(CategoryEntity category);

    // Async version of insertCategory
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Single<Long> insertCategoryAsync(CategoryEntity category);

    @Update
    void updateCategory(CategoryEntity category);

    // Async version of updateCategory
    @Update
    Single<Integer> updateCategoryAsync(CategoryEntity category);

    @Delete
    void deleteCategory(CategoryEntity category);

    // Async version of deleteCategory
    @Delete
    Single<Integer> deleteCategoryAsync(CategoryEntity category);
}