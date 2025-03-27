package com.example.expense_tracker.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;

import java.text.SimpleDateFormat;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ExpenseDetailFragment extends Fragment {
    private static final String TAG = "ExpenseDetailFragment";
    
    private String expenseId;
    private FirebaseAuth mAuth;
    private TextView expenseTitle, amountText, remarkText, createdDateText, createdByText;
    private Chip categoryChip;
    private ProgressBar progressBar;
    private FloatingActionButton fabAddExpense;
    private Expense currentExpense;

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
            expenseId = getArguments().getString("expense_id");
        }
        
        // Initialize views
        initializeViews(view);
        
        // Set up click listeners
        setupClickListeners();
        
        // Load expense details
        if (expenseId != null && !expenseId.isEmpty()) {
            loadExpenseDetails();
        } else {
            Toast.makeText(getContext(), "Invalid Expense ID", Toast.LENGTH_SHORT).show();
            requireActivity().onBackPressed();
        }
    }
    
    private void initializeViews(View view) {
        expenseTitle = view.findViewById(R.id.expenseTitle);
        amountText = view.findViewById(R.id.amountText);
        remarkText = view.findViewById(R.id.remarkText);
        categoryChip = view.findViewById(R.id.categoryChip);
        createdDateText = view.findViewById(R.id.createdDateText);
        createdByText = view.findViewById(R.id.createdByText);
        progressBar = view.findViewById(R.id.progressBar);
        fabAddExpense = view.findViewById(R.id.fabAddExpense);
    }
    
    private void setupClickListeners() {
        // Set up FAB
        fabAddExpense.setOnClickListener(v -> navigateToEditExpense());
    }
    
    private void showDeleteConfirmation() {
        // Create a confirmation dialog before deleting
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(requireContext());
        builder.setTitle("Confirm Delete");
        builder.setMessage("Are you sure you want to delete this expense?");
        builder.setPositiveButton("Delete", (dialog, which) -> deleteExpense());
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }
    
    private void navigateToEditExpense() {
        if (currentExpense == null) {
            Toast.makeText(getContext(), "Expense details not available", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Create a bundle with expense details
        Bundle args = new Bundle();
        args.putString("expense_id", expenseId);
        args.putString("title", currentExpense.getTitle());
        args.putDouble("amount", currentExpense.getAmount());
        args.putString("currency", currentExpense.getCurrency());
        args.putString("category", currentExpense.getCategory());
        args.putString("remark", currentExpense.getRemark());
        
        // Navigate to edit fragment (you will need to create this)
        // For now, show a toast
        Toast.makeText(getContext(), "Edit functionality coming soon", Toast.LENGTH_SHORT).show();
        
        // When you have an edit fragment, uncomment this code
        /*
        ExpenseEditFragment fragment = new ExpenseEditFragment();
        fragment.setArguments(args);
        
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
        */
    }
    
    private void loadExpenseDetails() {
        progressBar.setVisibility(View.VISIBLE);
        
        // Get the GUID using our utility class
        String dbGuid = GuidUtils.getUserDbGuid(requireContext());
        
        ExpenseApi expenseAPI = RetrofitClient.getClient().create(ExpenseApi.class);
        Call<Expense> call = expenseAPI.getExpense(dbGuid, expenseId);
        
        call.enqueue(new Callback<Expense>() {
            @Override
            public void onResponse(Call<Expense> call, Response<Expense> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    currentExpense = response.body();
                    displayExpenseDetails(currentExpense);
                } else {
                    try {
                        String errorBody = response.errorBody() != null ? response.errorBody().string() : "Unknown error";
                        Log.e(TAG, "Error loading expense: " + response.code() + " - " + errorBody);
                        Toast.makeText(getContext(), "Failed to load expense details", Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing error response", e);
                        Toast.makeText(getContext(), "Failed to load expense details", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<Expense> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Log.e(TAG, "API call failed", t);
                Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void displayExpenseDetails(Expense expense) {
        // Set the title (using title if available, otherwise category)
        String title = expense.getTitle();
        if (title == null || title.isEmpty()) {
            title = expense.getCategory();
        }
        expenseTitle.setText(title);
        
        // Set amount with currency
        amountText.setText(String.format(Locale.getDefault(), "%s %.2f", expense.getCurrency(), expense.getAmount()));
        
        // Set category chip
        categoryChip.setText(expense.getCategory());
        
        // Set remark if available
        if (expense.getRemark() != null && !expense.getRemark().isEmpty()) {
            remarkText.setText(expense.getRemark());
        } else {
            remarkText.setText("No remark");
        }
        
        // Format and set created date
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM dd, yyyy, h:mm a", Locale.getDefault());
            String formattedDate = dateFormat.format(expense.getCreatedDate());
            createdDateText.setText("Created on " + formattedDate);
        } catch (Exception e) {
            Log.e(TAG, "Error formatting date", e);
            createdDateText.setText("Created date not available");
        }
        
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
                        Log.e(TAG, "Error deleting expense: " + response.code() + " - " + errorBody);
                        Toast.makeText(getContext(), "Failed to delete expense", Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing error response", e);
                        Toast.makeText(getContext(), "Failed to delete expense", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Log.e(TAG, "API call failed", t);
                Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}