package com.example.expense_tracker.activities;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.expense_tracker.R;
import com.example.expense_tracker.models.Expense;
import com.google.android.material.button.MaterialButton;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class ExpenseDetailActivity extends AppCompatActivity {

    private TextView tvAmount;
    private TextView tvCurrency;
    private TextView tvCategory;
    private TextView tvRemark;
    private TextView tvCreatedDate;
    private TextView tvTimeAgo;
    private ImageView ivCategoryIcon;
    private ImageView ivReceipt;
    private MaterialButton btnEdit;
    private MaterialButton btnDelete;
    private MaterialButton btnDuplicate;
    private MaterialButton btnAddNewExpense;
    private MaterialButton btnBackToHome;

    private Expense expense;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_espense_detail);

        // Initialize UI components
        initializeViews();

        // Get expense data from intent
        if (getIntent().hasExtra("EXPENSE_DATA")) {
            expense = (Expense) getIntent().getSerializableExtra("EXPENSE_DATA");
            displayExpenseDetails();
        } else {
            Toast.makeText(this, "Error: No expense data found", Toast.LENGTH_SHORT).show();
            finish();
        }

        // Set click listeners for buttons
        setupClickListeners();
    }

    private void initializeViews() {
        tvAmount = findViewById(R.id.tvAmount);
        tvCurrency = findViewById(R.id.tvCurrency);
        tvCategory = findViewById(R.id.tvCategory);
        tvRemark = findViewById(R.id.tvRemark);
        tvCreatedDate = findViewById(R.id.tvCreatedDate);
        tvTimeAgo = findViewById(R.id.tvTimeAgo);
        ivCategoryIcon = findViewById(R.id.ivCategoryIcon);
        ivReceipt = findViewById(R.id.ivReceipt);
        btnEdit = findViewById(R.id.btnEdit);
        btnDelete = findViewById(R.id.btnDelete);
        btnDuplicate = findViewById(R.id.btnDuplicate);
        btnAddNewExpense = findViewById(R.id.btnAddNewExpense);
        btnBackToHome = findViewById(R.id.btnBackToHome);
    }

    private void displayExpenseDetails() {
        // Set expense details to UI components
        tvAmount.setText(String.format(Locale.getDefault(), "%.2f", expense.getAmount()));
        tvCurrency.setText(expense.getCurrency());
        tvCategory.setText(expense.getCategory());
        tvRemark.setText(expense.getRemark());
        tvCreatedDate.setText(expense.getCreatedDate());

        // Calculate time ago
        calculateTimeAgo(expense.getCreatedDate());

        // Set category icon based on category
        setCategoryIcon(expense.getCategory());
    }

    private void calculateTimeAgo(String createdDateStr) {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date createdDate = dateFormat.parse(createdDateStr);
            Date currentDate = new Date();

            if (createdDate != null) {
                long diffInMillis = currentDate.getTime() - createdDate.getTime();
                long days = TimeUnit.MILLISECONDS.toDays(diffInMillis);

                if (days == 0) {
                    tvTimeAgo.setText("Today");
                } else if (days == 1) {
                    tvTimeAgo.setText("Yesterday");
                } else if (days < 7) {
                    tvTimeAgo.setText(days + " days ago");
                } else if (days < 30) {
                    long weeks = days / 7;
                    tvTimeAgo.setText(weeks + (weeks == 1 ? " week ago" : " weeks ago"));
                } else {
                    long months = days / 30;
                    tvTimeAgo.setText(months + (months == 1 ? " month ago" : " months ago"));
                }
            }
        } catch (ParseException e) {
            tvTimeAgo.setText("Unknown");
        }
    }

    private void setCategoryIcon(String category) {
        // Set appropriate icon based on category
        switch (category.toLowerCase()) {
            case "food":
                ivCategoryIcon.setImageResource(R.drawable.ic_category);
                break;
            case "transport":
                ivCategoryIcon.setImageResource(R.drawable.ic_category);
                break;
            case "bills":
                ivCategoryIcon.setImageResource(R.drawable.ic_category);
                break;
            case "entertainment":
                ivCategoryIcon.setImageResource(R.drawable.ic_category);
                break;
            case "shopping":
                ivCategoryIcon.setImageResource(R.drawable.ic_category);
                break;
            default:
                ivCategoryIcon.setImageResource(R.drawable.ic_category);
                break;
        }
    }

    private void setupClickListeners() {
        // Edit button click listener
        btnEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ExpenseDetailActivity.this, AddExpenseActivity.class);
                intent.putExtra("EXPENSE_TO_EDIT", expense);
                startActivity(intent);
                finish(); // Close current activity
            }
        });

        // Delete button click listener
        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDeleteConfirmationDialog();
            }
        });

        // Duplicate button click listener
        btnDuplicate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ExpenseDetailActivity.this, AddExpenseActivity.class);
                intent.putExtra("EXPENSE_TO_DUPLICATE", expense);
                startActivity(intent);
                finish(); // Close current activity
            }
        });

        // Add new expense button click listener
        btnAddNewExpense.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ExpenseDetailActivity.this, AddExpenseActivity.class);
                startActivity(intent);
                finish(); // Close current activity
            }
        });

        // Back to home button click listener
        btnBackToHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // Just close this activity and return to MainActivity
            }
        });
    }

    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete Expense");
        builder.setMessage("Are you sure you want to delete this expense?");
        builder.setPositiveButton("Delete", (dialog, which) -> {
            // Here we would typically delete from database
            // For now, just show toast and return to MainActivity
            Toast.makeText(ExpenseDetailActivity.this, "Expense deleted", Toast.LENGTH_SHORT).show();
            finish();
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }
}