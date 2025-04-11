package com.example.expense_tracker.fragments;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
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

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ExpenseEditFragment extends Fragment {
    private static final String TAG = "ExpenseEditFragment";

    // UI Components
    private EditText titleEditText, amountEditText, remarkEditText;
    private Spinner currencySpinner, categorySpinner;
    private Button saveButton, cancelButton;
    private ProgressBar progressBar;

    // Data
    private String expenseId;
    private FirebaseAuth mAuth;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_expense_edit, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Set the title for the fragment
        requireActivity().setTitle("Edit Expense");

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Initialize views
        initializeViews(view);

        // Setup spinners
        setupSpinners();

        // Get expense data from arguments
        if (getArguments() != null) {
            expenseId = getArguments().getString("expense_id");
            populateFields();
        } else {
            Toast.makeText(getContext(), "No expense data provided", Toast.LENGTH_SHORT).show();
            requireActivity().onBackPressed();
        }

        // Set up button click listeners
        setupClickListeners();
    }

    private void initializeViews(View view) {
        titleEditText = view.findViewById(R.id.editTextTitle);
        amountEditText = view.findViewById(R.id.editTextAmount);
        remarkEditText = view.findViewById(R.id.editTextRemark);
        currencySpinner = view.findViewById(R.id.currencySpinner);
        categorySpinner = view.findViewById(R.id.categorySpinner);
        saveButton = view.findViewById(R.id.buttonSave);
        cancelButton = view.findViewById(R.id.buttonCancel);
        progressBar = view.findViewById(R.id.progressBar);
    }

    private void setupSpinners() {
        // Setup currency spinner
        ArrayAdapter<CharSequence> currencyAdapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.currency_options,
                android.R.layout.simple_spinner_item);
        currencyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        currencySpinner.setAdapter(currencyAdapter);

        // Setup category spinner
        ArrayAdapter<CharSequence> categoryAdapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.category_options,
                android.R.layout.simple_spinner_item);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(categoryAdapter);
    }

    private void populateFields() {
        if (getArguments() == null)
            return;

        // Set fields with data from arguments
        String title = getArguments().getString("title");
        double amount = getArguments().getDouble("amount");
        String currency = getArguments().getString("currency");
        String category = getArguments().getString("category");
        String remark = getArguments().getString("remark");

        // Populate the EditText fields
        if (!TextUtils.isEmpty(title)) {
            titleEditText.setText(title);
        }

        amountEditText.setText(String.valueOf(amount));

        if (!TextUtils.isEmpty(remark)) {
            remarkEditText.setText(remark);
        }

        // Set spinner selections
        setSpinnerSelection(currencySpinner, currency);
        setSpinnerSelection(categorySpinner, category);
    }

    private void setSpinnerSelection(Spinner spinner, String value) {
        if (value == null || value.isEmpty())
            return;

        ArrayAdapter adapter = (ArrayAdapter) spinner.getAdapter();
        for (int i = 0; i < adapter.getCount(); i++) {
            if (adapter.getItem(i).toString().equals(value)) {
                spinner.setSelection(i);
                break;
            }
        }
    }

    private void setupClickListeners() {
        saveButton.setOnClickListener(v -> saveExpense());

        cancelButton.setOnClickListener(v -> requireActivity().onBackPressed());
    }

    private void saveExpense() {
        // Validate input
        if (!validateInput()) {
            return;
        }

        // Show progress
        progressBar.setVisibility(View.VISIBLE);

        // Get the data from form fields
        String title = titleEditText.getText().toString().trim();
        double amount;
        try {
            amount = Double.parseDouble(amountEditText.getText().toString().trim());
        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "Invalid amount format", Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.GONE);
            return;
        }

        String currency = currencySpinner.getSelectedItem().toString();
        String category = categorySpinner.getSelectedItem().toString();
        String remark = remarkEditText.getText().toString().trim();

        // Get database GUID
        String dbGuid = GuidUtils.getUserDbGuid(requireContext());

        // Create expense object for update
        Expense updatedExpense = new Expense();
        updatedExpense.setId(expenseId);
        updatedExpense.setTitle(title);
        updatedExpense.setAmount(amount);
        updatedExpense.setCurrency(currency);
        updatedExpense.setCategory(category);
        updatedExpense.setRemark(remark);

        // Call API to update expense
        ExpenseApi expenseApi = RetrofitClient.getClient().create(ExpenseApi.class);
        Call<Expense> call = expenseApi.updateExpense(dbGuid, expenseId, updatedExpense);

        call.enqueue(new Callback<Expense>() {
            @Override
            public void onResponse(Call<Expense> call, Response<Expense> response) {
                progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "Expense updated successfully: " + response.body().getId());
                    Toast.makeText(getContext(), "Expense updated successfully", Toast.LENGTH_SHORT).show();

                    // Return to previous screen
                    requireActivity().onBackPressed();
                } else {
                    Log.e(TAG, "Failed to update expense. Code: " + response.code());
                    try {
                        if (response.errorBody() != null) {
                            Log.e(TAG, "Error body: " + response.errorBody().string());
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing error response", e);
                    }

                    Toast.makeText(getContext(), "Failed to update expense", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Expense> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Log.e(TAG, "Error updating expense", t);
                Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean validateInput() {
        boolean isValid = true;

        // Validate amount (required)
        String amountStr = amountEditText.getText().toString().trim();
        if (TextUtils.isEmpty(amountStr)) {
            amountEditText.setError("Amount is required");
            isValid = false;
        } else {
            try {
                double amount = Double.parseDouble(amountStr);
                if (amount <= 0) {
                    amountEditText.setError("Amount must be greater than 0");
                    isValid = false;
                }
            } catch (NumberFormatException e) {
                amountEditText.setError("Invalid amount format");
                isValid = false;
            }
        }

        // Either title or category must be provided
        String title = titleEditText.getText().toString().trim();
        String category = categorySpinner.getSelectedItem().toString();

        if (TextUtils.isEmpty(title) && (TextUtils.isEmpty(category) || category.equals("Select Category"))) {
            titleEditText.setError("Either title or category must be provided");
            isValid = false;
        }

        return isValid;
    }
}
