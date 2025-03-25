package com.example.expense_tracker.routes;

import com.example.expense_tracker.models.Category;
import com.example.expense_tracker.models.Expense;
import com.example.expense_tracker.models.User;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Interface defining the API endpoints for the Expense Tracker application.
 * Uses Retrofit annotations to map HTTP requests.
 */
public interface ApiService {

    // Expense related endpoints
    @GET("expenses")
    Call<List<Expense>> getExpenses(@Header("Authorization") String authToken);
    
    @GET("expenses/{id}")
    Call<Expense> getExpenseById(@Header("Authorization") String authToken, @Path("id") String expenseId);
    
    @POST("expenses")
    Call<Expense> createExpense(@Header("Authorization") String authToken, @Body Expense expense);
    
    @PUT("expenses/{id}")
    Call<Expense> updateExpense(@Header("Authorization") String authToken, @Path("id") String expenseId, @Body Expense expense);
    
    @DELETE("expenses/{id}")
    Call<Void> deleteExpense(@Header("Authorization") String authToken, @Path("id") String expenseId);
    
    // Category related endpoints
    @GET("categories")
    Call<List<Category>> getCategories(@Header("Authorization") String authToken);
    
    @POST("categories")
    Call<Category> createCategory(@Header("Authorization") String authToken, @Body Category category);
    
    // Analytics and reportingD
    @GET("analytics/monthly")
    Call<Map<String, Object>> getMonthlyReport(@Header("Authorization") String authToken, @Query("month") int month, @Query("year") int year);
    
    @GET("analytics/category")
    Call<Map<String, Object>> getCategoryReport(@Header("Authorization") String authToken, @Query("startDate") String startDate, @Query("endDate") String endDate);
    
    // User profile management
    @GET("user/profile")
    Call<User> getUserProfile(@Header("Authorization") String authToken);
    
    @PUT("user/profile")
    Call<User> updateUserProfile(@Header("Authorization") String authToken, @Body User user);
    
    // Budget management
    @GET("budget")
    Call<Map<String, Object>> getBudget(@Header("Authorization") String authToken);
    
    @POST("budget")
    Call<Map<String, Object>> setBudget(@Header("Authorization") String authToken, @Body Map<String, Object> budgetData);
}

