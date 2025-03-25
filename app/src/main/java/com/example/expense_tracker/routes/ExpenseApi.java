package com.example.expense_tracker.routes;

import com.example.expense_tracker.models.Expense;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface ExpenseApi {
    @GET("expenses")
    Call<List<Expense>> getExpenses(@Header("Authorization") String authToken);
    
    @GET("expenses/{id}")
    Call<Expense> getExpense(@Header("Authorization") String authToken, @Path("id") long id);
    
    @POST("expenses")
    Call<Expense> createExpense(@Header("Authorization") String authToken, @Body Expense expense);
    
    @DELETE("expenses/{id}")
    Call<Void> deleteExpense(@Header("Authorization") String authToken, @Path("id") long id);
}
