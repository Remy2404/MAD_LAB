package com.example.expense_tracker.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.expense_tracker.R;
import com.example.expense_tracker.models.Expense;
import com.example.expense_tracker.routes.ExpenseApi;
import com.example.expense_tracker.routes.RetrofitClient;
import com.example.expense_tracker.utils.GuidUtils;
import com.google.android.material.chip.Chip;
import com.google.firebase.auth.FirebaseAuth;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.UUID;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ExpenseDetailFragment extends Fragment {

    private String expenseId; // Change type to String
    private FirebaseAuth mAuth;
    private TextView expenseTitle, amountText, remarkText, createdDateText, createdByText;
    private Chip categoryChip;
    private ProgressBar progressBar;
    private ImageButton backButton, deleteButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                            Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_expense_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        
        // Get expense ID from arguments
        if (getArguments() != null) {
            expenseId = getArguments().getString("expense_id"); // Get String ID
        }
        
        // Initialize views
        expenseTitle = view.findViewById(R.id.expenseTitle);
        amountText = view.findViewById(R.id.amountText);
        remarkText = view.findViewById(R.id.remarkText);
        categoryChip = view.findViewById(R.id.categoryChip);
        createdDateText = view.findViewById(R.id.createdDateText);
        createdByText = view.findViewById(R.id.createdByText);
        progressBar = view.findViewById(R.id.progressBar);
        backButton = view.findViewById(R.id.backButton);
        deleteButton = view.findViewById(R.id.deleteButton);
        
        // Set up back button
        backButton.setOnClickListener(v -> requireActivity().onBackPressed());
        
        // Set up delete button
        deleteButton.setOnClickListener(v -> deleteExpense());
        
        // Load expense details
        if (expenseId != null && !expenseId.isEmpty()) { // Check if ID is valid
            loadExpenseDetails();
        } else {
            Toast.makeText(getContext(), "Invalid Expense ID", Toast.LENGTH_SHORT).show();
            // Optionally navigate back or show an error state
        }
    }
    
    private void loadExpenseDetails() {
        progressBar.setVisibility(View.VISIBLE);
        
        // Get the GUID using our utility class
        String dbGuid = GuidUtils.getUserDbGuid(requireContext());
        
        ExpenseApi expenseAPI = RetrofitClient.getClient().create(ExpenseApi.class);
        // Pass the String expenseId directly to the API call
        Call<Expense> call = expenseAPI.getExpense(dbGuid, expenseId);
        
        call.enqueue(new Callback<Expense>() {
            @Override
            public void onResponse(Call<Expense> call, Response<Expense> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    displayExpenseDetails(response.body());
                } else {
                    try {
                        String errorBody = response.errorBody() != null ? response.errorBody().string() : "Unknown error";
                        Log.e("ExpenseDetailFragment", "Error loading expense: " + response.code() + " - " + errorBody);
                        Toast.makeText(getContext(), "Failed to load expense details", Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Log.e("ExpenseDetailFragment", "Error parsing error response", e);
                        Toast.makeText(getContext(), "Failed to load expense details", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<Expense> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Log.e("ExpenseDetailFragment", "API call failed", t);
                Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void displayExpenseDetails(Expense expense) {
        // Set the title (using category as title if available)
        String title = expense.getCategory();
        expenseTitle.setText(title);
        
        // Set amount with currency
        amountText.setText(String.format("%s %.2f", expense.getCurrency(), expense.getAmount()));
        
        // Set category chip
        categoryChip.setText(expense.getCategory());
        
        // Set remark if available
        if (expense.getRemark() != null && !expense.getRemark().isEmpty()) {
            remarkText.setText(expense.getRemark());
        } else {
            remarkText.setText("No remark");
        }
        
        // Format and set created date
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM dd, yyyy, h:mm a", Locale.getDefault());
        String formattedDate = dateFormat.format(expense.getCreatedDate());
        createdDateText.setText("Created on " + formattedDate);
        
        // Set created by
        createdByText.setText("Created by " + expense.getCreatedBy());
    }
    
    private void deleteExpense() {
        progressBar.setVisibility(View.VISIBLE);
        
        // Get the GUID using our utility class
        String dbGuid = GuidUtils.getUserDbGuid(requireContext());
        
        ExpenseApi expenseAPI = RetrofitClient.getClient().create(ExpenseApi.class);
        // Pass the String expenseId directly to the API call
        Call<Void> call = expenseAPI.deleteExpense(dbGuid, expenseId);
        
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "Expense deleted successfully", Toast.LENGTH_SHORT).show();
                    requireActivity().onBackPressed();
                } else {
                    try {
                        String errorBody = response.errorBody() != null ? response.errorBody().string() : "Unknown error";
                        Log.e("ExpenseDetailFragment", "Error deleting expense: " + response.code() + " - " + errorBody);
                        Toast.makeText(getContext(), "Failed to delete expense", Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Log.e("ExpenseDetailFragment", "Error parsing error response", e);
                        Toast.makeText(getContext(), "Failed to delete expense", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Log.e("ExpenseDetailFragment", "API call failed", t);
                Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}