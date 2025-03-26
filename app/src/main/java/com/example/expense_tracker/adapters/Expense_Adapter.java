package com.example.expense_tracker.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.expense_tracker.R;
import com.example.expense_tracker.activities.MainActivity;
import com.example.expense_tracker.models.Expense;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class Expense_Adapter extends RecyclerView.Adapter<Expense_Adapter.ExpenseViewHolder> {
    private static final String TAG = "Expense_Adapter";
    private Context context;
    private List<Expense> expenses;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());

    public Expense_Adapter(Context context, List<Expense> expenses) {
        this.context = context;
        this.expenses = expenses;
    }

    @NonNull
    @Override
    public ExpenseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        try {
            View view = LayoutInflater.from(context).inflate(R.layout.item_expense, parent, false);
            return new ExpenseViewHolder(view);
        } catch (Exception e) {
            Log.e(TAG, "Error inflating item_expense layout: " + e.getMessage());
            // Fallback to create a basic view if the layout fails to inflate
            View fallbackView = new View(context);
            fallbackView.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
            return new ExpenseViewHolder(fallbackView);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull ExpenseViewHolder holder, int position) {
        try {
            Expense expense = expenses.get(position);
            
            // Check for null views or missing data
            if (holder.tvCategory != null && expense.getCategory() != null) {
                holder.tvCategory.setText(expense.getCategory());
            }
            
            if (holder.tvAmount != null) {
                String currency = expense.getCurrency() != null ? expense.getCurrency() : "$";
                holder.tvAmount.setText(String.format("%s %.2f", currency, expense.getAmount()));
            }
            
            if (holder.tvCreateDate != null && expense.getCreatedDate() != null) {
                holder.tvCreateDate.setText(dateFormat.format(expense.getCreatedDate()));
            }
            
            holder.itemView.setOnClickListener(v -> openExpenseDetail(expense));
        } catch (Exception e) {
            Log.e(TAG, "Error binding expense data: " + e.getMessage());
        }
    }

    @Override
    public int getItemCount() {
        return expenses != null ? expenses.size() : 0;
    }
    private void openExpenseDetail(Expense expense) {
        if (context instanceof MainActivity) {
            try {
                String expenseId = expense.getId(); // Get the ID as String
                if (expenseId == null || expenseId.isEmpty()) {
                    throw new IllegalArgumentException("Expense ID is null or empty");
                }
                ((MainActivity) context).navigateToExpenseDetail(expenseId); // Pass the String ID
            } catch (IllegalArgumentException e) {
                Log.e(TAG, "Invalid expense ID: " + e.getMessage());
                Toast.makeText(context, "Invalid expense ID", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Log.e(TAG, "Error opening expense details: " + e.getMessage());
                Toast.makeText(context, "Could not open expense details", Toast.LENGTH_SHORT).show();
            }
        }
    }


    static class ExpenseViewHolder extends RecyclerView.ViewHolder {
        TextView tvCategory, tvAmount, tvCreateDate, tvCurrency, tvRemark;

        public ExpenseViewHolder(@NonNull View itemView) {
            super(itemView);
            try {
                tvCategory = itemView.findViewById(R.id.tvCategory);
                tvAmount = itemView.findViewById(R.id.tvAmount);
                tvCreateDate = itemView.findViewById(R.id.tvCreateDate); // Fixed ID reference
                tvCurrency = itemView.findViewById(R.id.tvCurrency);
                tvRemark = itemView.findViewById(R.id.tvRemark);
            } catch (Exception e) {
                Log.e("ExpenseViewHolder", "Error finding views: " + e.getMessage());
                // Leave views as null, they'll be checked before use
            }
        }
    }
}
