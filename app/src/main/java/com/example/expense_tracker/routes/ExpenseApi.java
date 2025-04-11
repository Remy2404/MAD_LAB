package com.example.expense_tracker.routes;

import com.example.expense_tracker.models.Expense;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface ExpenseApi {
    @GET("expenses")
    Call<List<Expense>> getExpenses(@Header("X-DB-NAME") String dbName);

    @GET("expenses/{id}")
    Call<Expense> getExpense(@Header("X-DB-NAME") String dbName, @Path("id") String id);

    @POST("expenses")
    Call<Expense> createExpense(@Header("X-DB-NAME") String dbName, @Body Expense expense);

    @PUT("expenses/{id}")
    Call<Expense> updateExpense(
            @Header("X-DB-NAME") String dbName,
            @Path("id") String id,
            @Body Expense expense);

    @DELETE("expenses/{id}")
    Call<Void> deleteExpense(@Header("X-DB-NAME") String dbName, @Path("id") String id);

}