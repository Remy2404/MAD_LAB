package com.example.expense_tracker.fragments;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.expense_tracker.R;
import com.example.expense_tracker.routes.ExpenseApi;
import com.example.expense_tracker.routes.RetrofitClient;
import com.google.firebase.auth.FirebaseAuth;

public class HomeFragment extends Fragment {
    private FirebaseAuth mAuth;
    private TextView totalExpenseText;
    private TextView totalIncomeText;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragement_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mAuth = FirebaseAuth.getInstance();

        // Initialize views
        totalExpenseText = view.findViewById(R.id.totalExpenseText);
        totalIncomeText = view.findViewById(R.id.totalIncomeText);

        // Load dashboard data
        loadDashboardData();
    }

    private void loadDashboardData() {
        ExpenseApi expenseAPI = RetrofitClient.getClient().create(ExpenseApi.class);
        String authToken = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : "";

        // Here you would make API calls to get total expense and income
        // For now, we'll use the static values from the layout
        // You can implement the actual API calls later
    }
}
