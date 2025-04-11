package com.example.expense_tracker.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.expense_tracker.R;
import com.example.expense_tracker.models.Expense;
import com.google.android.material.card.MaterialCardView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ExpenseAdapter extends RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder> {

    private final Context context;
    private List<Expense> expenseList;
    private final OnExpenseClickListener listener;

    // Interface for click listeners
    public interface OnExpenseClickListener {
        void onExpenseClick(Expense expense, int position);

        void onDeleteClick(Expense expense, int position);

        void onIncreaseQuantity(Expense expense, int position);

        void onDecreaseQuantity(Expense expense, int position);
    }

    public ExpenseAdapter(Context context, OnExpenseClickListener listener) {
        this.context = context;
        this.expenseList = new ArrayList<>();
        this.listener = listener;
    }

    public void setExpenses(List<Expense> expenses) {
        this.expenseList = expenses;
        notifyDataSetChanged();
    }

    public List<Expense> getExpenseList() {
        return expenseList;
    }

    public void removeItem(int position) {
        if (position >= 0 && position < expenseList.size()) {
            expenseList.remove(position);
            notifyItemRemoved(position);
        }
    }

    @NonNull
    @Override
    public ExpenseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_expense, parent, false);
        return new ExpenseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ExpenseViewHolder holder, int position) {
        Expense expense = expenseList.get(position);

        // Set category
        holder.tvCategory.setText(expense.getCategory());

        // Set amount with currency
        holder.tvAmount.setText(String.format(Locale.getDefault(), "%.2f", expense.getAmount()));
        holder.tvCurrency.setText(expense.getCurrency());

        // Set remark if available
        if (expense.getRemark() != null && !expense.getRemark().isEmpty()) {
            holder.tvRemark.setText(expense.getRemark());
            holder.tvRemark.setVisibility(View.VISIBLE);
        } else {
            holder.tvRemark.setVisibility(View.GONE);
        }

        // Format and set created date
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
            String formattedDate = dateFormat.format(expense.getCreatedDate());
            holder.tvCreateDate.setText(formattedDate);
        } catch (Exception e) {
            holder.tvCreateDate.setText("N/A");
        }

        // Set quantity (if applicable)
        int quantity = expense.getQuantity() != null ? expense.getQuantity() : 1;
        holder.tvQuantity.setText(String.valueOf(quantity));
        // Show/hide quantity controls based on whether quantity is relevant (optional)
        // holder.quantityLayout.setVisibility(quantity > 0 ? View.VISIBLE : View.GONE);

        // Set icon based on category (simplified example)
        // You would have more complex logic here based on your categories
        switch (expense.getCategory().toLowerCase()) {
            case "food":
                holder.ivExpenseIcon.setImageResource(R.drawable.ic_food);
                break;
            case "transportation":
                holder.ivExpenseIcon.setImageResource(R.drawable.ic_transport);
                break;
            case "shopping":
                holder.ivExpenseIcon.setImageResource(R.drawable.ic_shopping);
                break;
            default:
                holder.ivExpenseIcon.setImageResource(R.drawable.ic_category);
                break;
        }

        // Setup click listeners
        holder.container.setOnClickListener(v -> {
            if (listener != null) {
                listener.onExpenseClick(expense, holder.getAdapterPosition());
            }
        });

        holder.btnDecrease.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDecreaseQuantity(expense, holder.getAdapterPosition());
            }
        });

        holder.btnIncrease.setOnClickListener(v -> {
            if (listener != null) {
                listener.onIncreaseQuantity(expense, holder.getAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return expenseList.size();
    }

    public static class ExpenseViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView container; // Ensure this is MaterialCardView
        ImageView ivExpenseIcon;
        TextView tvCategory, tvAmount, tvCurrency, tvCreateDate, tvRemark, tvQuantity;
        ImageButton btnIncrease, btnDecrease;
        View quantityLayout;

        public ExpenseViewHolder(@NonNull View itemView) {
            super(itemView);
            // Ensure the cast here matches the type declared above
            container = (MaterialCardView) itemView.findViewById(R.id.container);
            ivExpenseIcon = itemView.findViewById(R.id.ivExpenseIcon);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            tvAmount = itemView.findViewById(R.id.tvAmount);
            tvCurrency = itemView.findViewById(R.id.tvCurrency);
            tvCreateDate = itemView.findViewById(R.id.tvCreateDate);
            tvRemark = itemView.findViewById(R.id.tvRemark);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
            btnIncrease = itemView.findViewById(R.id.btnIncreaseQuantity);
            btnDecrease = itemView.findViewById(R.id.btnDecreaseQuantity);
            quantityLayout = itemView.findViewById(R.id.quantityLayout);
        }
    }
}