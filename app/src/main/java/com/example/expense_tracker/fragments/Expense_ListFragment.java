package com.example.expense_tracker.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.expense_tracker.R;
import com.example.expense_tracker.adapters.Expense_Adapter;
import com.example.expense_tracker.models.Expense;
import com.example.expense_tracker.routes.ExpenseApi;
import com.example.expense_tracker.routes.RetrofitClient;
import com.example.expense_tracker.utils.GuidUtils;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Expense_ListFragment extends Fragment {

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private Expense_Adapter adapter;
    private List<Expense> expenses = new ArrayList<>();
    private FirebaseAuth mAuth;
    private static final String TAG = "Expense_ListFragment";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_ex_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Set the title for the fragment
        requireActivity().setTitle("Expense List");

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Initialize views
        recyclerView = view.findViewById(R.id.recyclerView);
        progressBar = view.findViewById(R.id.progressBar);

        // Setup RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        // Changed: Removed the lambda argument to match Expense_Adapter constructor's parameters.
        adapter = new Expense_Adapter(requireContext(), expenses);
        recyclerView.setAdapter(adapter);

        // Load expenses
        loadExpenses();
    }

    private void loadExpenses() {
        progressBar.setVisibility(View.VISIBLE);

        try {
            // Get a valid GUID from GuidUtils
            String dbGuid = GuidUtils.getUserDbGuid(requireContext());
            Log.d(TAG, "Using GUID for API call: " + dbGuid);

            ExpenseApi expenseAPI = RetrofitClient.getClient().create(ExpenseApi.class);
            Call<List<Expense>> call = expenseAPI.getExpenses(dbGuid);

            call.enqueue(new Callback<List<Expense>>() {
                @Override
                public void onResponse(Call<List<Expense>> call, Response<List<Expense>> response) {
                    progressBar.setVisibility(View.GONE);

                    if (response.isSuccessful() && response.body() != null) {
                        expenses.clear();
                        expenses.addAll(response.body());
                        adapter.notifyDataSetChanged();

                        if (expenses.isEmpty()) {
                            Toast.makeText(getContext(), "No expenses found", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        try {
                            String errorBody = response.errorBody() != null ? response.errorBody().string() : "Unknown error";
                            Log.e(TAG, "Error loading expenses: " + response.code() + " - " + errorBody);
                            Toast.makeText(getContext(), "Failed to load expenses: " + response.code(), Toast.LENGTH_SHORT).show();
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing error response", e);
                            Toast.makeText(getContext(), "Failed to load expenses", Toast.LENGTH_SHORT).show();
                        }
                    }
                }

                @Override
                public void onFailure(Call<List<Expense>> call, Throwable t) {
                    progressBar.setVisibility(View.GONE);
                    Log.e(TAG, "API call failed", t);
                    Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            progressBar.setVisibility(View.GONE);
            Log.e(TAG, "Exception in loadExpenses", e);
            Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
