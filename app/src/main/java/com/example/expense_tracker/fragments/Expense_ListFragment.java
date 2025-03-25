package com.example.expense_tracker.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Expense_ListFragment extends Fragment {

    private RecyclerView recyclerView;
    private Expense_Adapter adapter;
    private List<Expense> expenses = new ArrayList<>();
    private FloatingActionButton fabAddExpense;
    private FirebaseAuth mAuth;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_ex_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        
        // Set user ID as database name for Retrofit
        if (mAuth.getCurrentUser() != null) {
            RetrofitClient.setDbName(mAuth.getCurrentUser().getUid());
        }
        
        // Initialize RecyclerView
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new Expense_Adapter(getContext(), expenses);
        recyclerView.setAdapter(adapter);
        
        // Set up FAB for adding new expenses
        fabAddExpense = view.findViewById(R.id.fabAddExpense);
        fabAddExpense.setOnClickListener(v -> openAddExpenseFragment());
        
        // Load expenses from API
        loadExpenses();
    }
    
    private void loadExpenses() {
        ExpenseApi expenseAPI = RetrofitClient.getClient().create(ExpenseApi.class);
        
        // Get the auth token
        String authToken = "";
        if (mAuth.getCurrentUser() != null) {
            authToken = mAuth.getCurrentUser().getUid();
        }
        
        Call<List<Expense>> call = expenseAPI.getExpenses(authToken);
        
        call.enqueue(new Callback<List<Expense>>() {
            @Override
            public void onResponse(Call<List<Expense>> call, Response<List<Expense>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    expenses.clear();
                    expenses.addAll(response.body());
                    adapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(getContext(), "Failed to load expenses", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Expense>> call, Throwable t) {
                Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void openAddExpenseFragment() {
        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new AddExpense_Fragment())
                .addToBackStack(null)
                .commit();
    }
}