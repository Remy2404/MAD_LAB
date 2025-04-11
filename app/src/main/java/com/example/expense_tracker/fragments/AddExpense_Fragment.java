package com.example.expense_tracker.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.expense_tracker.R;
import com.example.expense_tracker.models.Expense;
import com.example.expense_tracker.routes.ExpenseApi;
import com.example.expense_tracker.routes.RetrofitClient;
import com.example.expense_tracker.utils.GuidUtils;
import com.google.firebase.auth.FirebaseAuth;

import java.io.IOException;
import java.util.Date;
import java.util.UUID;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddExpense_Fragment extends Fragment {

    private EditText etAmount, etRemark, etTitle;
    private Spinner spinnerCurrency, spinnerCategory;
    private Button btnSave, btnCancel;
    private FirebaseAuth mAuth;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_add_expense, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Set the title for the fragment
        requireActivity().setTitle("Add Expense");
        
        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        
        // Initialize views
        etAmount = view.findViewById(R.id.etAmount);
        etRemark = view.findViewById(R.id.etRemark);
        etTitle = view.findViewById(R.id.etTitle);
        spinnerCurrency = view.findViewById(R.id.spinnerCurrency);
        spinnerCategory = view.findViewById(R.id.spinnerCategory);
        btnSave = view.findViewById(R.id.btnSave);
        btnCancel = view.findViewById(R.id.btnCancel);
        
        // Set up currency spinner
        ArrayAdapter<CharSequence> currencyAdapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.currency_options,
                android.R.layout.simple_spinner_item
        );
        currencyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCurrency.setAdapter(currencyAdapter);
        
        // Set up category spinner
        ArrayAdapter<CharSequence> categoryAdapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.categories,
                android.R.layout.simple_spinner_item
        );
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(categoryAdapter);
        
        // Set up button click listeners
        btnSave.setOnClickListener(v -> saveExpense());
        btnCancel.setOnClickListener(v -> getParentFragmentManager().popBackStack());
    }
    
    private void saveExpense() {
        try {
            // Validate input
            String title = etTitle.getText().toString().trim();
            String amountStr = etAmount.getText().toString().trim();
            
            if (title.isEmpty()) {
                etTitle.setError("Title is required");
                return;
            }
            
            if (amountStr.isEmpty()) {
                etAmount.setError("Amount is required");
                return;
            }
            
            double amount = Double.parseDouble(amountStr);
            String currency = spinnerCurrency.getSelectedItem().toString();
            String category = spinnerCategory.getSelectedItem().toString();
            String remark = etRemark.getText().toString().trim();
            
            // Create expense object
            Expense expense = new Expense();
            expense.setId(UUID.randomUUID().toString());
            expense.setTitle(title);
            expense.setAmount(amount);
            expense.setCurrency(currency);
            expense.setCategory(category);
            expense.setRemark(remark);
            expense.setCreatedDate(new Date());
            
            if (mAuth.getCurrentUser() != null) {
                String userId = mAuth.getCurrentUser().getUid();
                expense.setCreatedBy(userId);
                
                // Get the GUID using our utility class
                String dbGuid = GuidUtils.getUserDbGuid(requireContext());
                
                // Send POST request to API
                ExpenseApi expenseAPI = RetrofitClient.getClient().create(ExpenseApi.class);
                Call<Expense> call = expenseAPI.createExpense(dbGuid, expense);
                
                call.enqueue(new Callback<Expense>() {
                    @Override
                    public void onResponse(Call<Expense> call, Response<Expense> response) {
                        if (response.isSuccessful()) {
                            Toast.makeText(getContext(), "Expense saved successfully", Toast.LENGTH_SHORT).show();
                            getParentFragmentManager().popBackStack();
                        } else {
                            try {
                                String errorBody = response.errorBody().string();
                                Log.e("API Error", "Error code: " + response.code() + ", body: " + errorBody);
                                Toast.makeText(getContext(), "Failed to save expense: " + response.code(), Toast.LENGTH_SHORT).show();
                            } catch (IOException e) {
                                Log.e("API Error", "Error parsing error response", e);
                                Toast.makeText(getContext(), "Failed to save expense", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<Expense> call, Throwable t) {
                        Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                Toast.makeText(getContext(), "User is not logged in", Toast.LENGTH_SHORT).show();
            }
        } catch (NumberFormatException e) {
            etAmount.setError("Invalid amount");
        }
    }
}