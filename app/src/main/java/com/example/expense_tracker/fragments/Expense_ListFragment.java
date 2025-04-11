package com.example.expense_tracker.fragments;

import android.graphics.Canvas;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.expense_tracker.R;
import com.example.expense_tracker.activities.MainActivity;
import com.example.expense_tracker.adapters.ExpenseAdapter;
import com.example.expense_tracker.models.Expense;
import com.example.expense_tracker.routes.ExpenseApi;
import com.example.expense_tracker.routes.RetrofitClient;
import com.example.expense_tracker.utils.GuidUtils;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Expense_ListFragment extends Fragment implements ExpenseAdapter.OnExpenseClickListener {

    private RecyclerView recyclerView;
    private ExpenseAdapter adapter;
    private List<Expense> expenses = new ArrayList<>();
    private FirebaseAuth mAuth;
    private static final String TAG = "Expense_ListFragment";
    private ProgressBar progressBar;
    private TextView tvEmptyState;
    private FloatingActionButton fabAddExpense;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_expense_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Set the title for the fragment
        requireActivity().setTitle("Expense List");

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Initialize views - Ensure IDs match fragment_expense_list.xml
        recyclerView = view.findViewById(R.id.recyclerViewExpenses); // Verify this ID
        progressBar = view.findViewById(R.id.progressBar); // Verify this ID
        tvEmptyState = view.findViewById(R.id.tvEmptyState); // Verify this ID
        fabAddExpense = view.findViewById(R.id.fabAddExpense); // Verify this ID

        // Setup RecyclerView with adapter
        adapter = new ExpenseAdapter(requireContext(), this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        // Set FAB click listener to navigate to add expense screen
        fabAddExpense.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).loadFragment(new AddExpense_Fragment(), "AddExpense_Fragment");
            }
        });

        // Load expenses
        loadExpenses();

        // Setup swipe to delete with enhanced animations
        setupSwipeToDelete();
    }

    private void setupSwipeToDelete() {
        ItemTouchHelper.SimpleCallback itemTouchHelperCallback = new ItemTouchHelper.SimpleCallback(0,
                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            private final float swipeThreshold = 0.3f; // Swipe threshold for activation
            private final float swipeEscapeVelocity = 0.5f; // Velocity needed to trigger swipe

            @Override
            public float getSwipeThreshold(@NonNull RecyclerView.ViewHolder viewHolder) {
                return swipeThreshold;
            }

            @Override
            public float getSwipeEscapeVelocity(float defaultValue) {
                return swipeEscapeVelocity * defaultValue;
            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView,
                    @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY,
                    int actionState, boolean isCurrentlyActive) {

                View itemView = viewHolder.itemView;

                // Simplified swipe animation: Just scale down slightly
                float swipeProgress = Math.min(Math.abs(dX) / (float) itemView.getWidth(), 1.0f);
                float scale = 1.0f - (0.05f * swipeProgress);
                itemView.setScaleY(scale);

                // Reset rotation if previously applied
                itemView.setRotation(0);

                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder,
                    @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                // Check if position is valid before proceeding
                if (position == RecyclerView.NO_POSITION || position >= adapter.getExpenseList().size()) {
                    Log.w(TAG, "Invalid position in onSwiped: " + position);
                    adapter.notifyDataSetChanged(); // Refresh adapter state just in case
                    return;
                }
                final Expense expenseItem = adapter.getExpenseList().get(position);

                // Temporarily remove the item from the adapter
                adapter.removeItem(position);

                // Show a Snackbar with undo option
                Snackbar snackbar = Snackbar.make(recyclerView,
                        direction == ItemTouchHelper.LEFT ? "Expense deleted" : "Navigating to details...", // Updated
                                                                                                            // message
                                                                                                            // for right
                                                                                                            // swipe
                        Snackbar.LENGTH_LONG);

                snackbar.setAction("UNDO", view -> {
                    // Restore the item if user clicks UNDO
                    // Check if the item still exists in the main list (could be modified elsewhere)
                    if (!expenses.contains(expenseItem)) {
                        // Add it back to the main list first
                        if (position <= expenses.size()) {
                            expenses.add(position, expenseItem);
                        } else {
                            expenses.add(expenseItem); // Add to end if position is invalid
                        }
                    }
                    // Update the adapter's list and notify
                    adapter.setExpenses(new ArrayList<>(expenses)); // Refresh adapter list
                    adapter.notifyItemInserted(position);
                    // Optional: Scroll to the restored item
                    recyclerView.scrollToPosition(position);
                });

                snackbar.addCallback(new Snackbar.Callback() {
                    @Override
                    public void onDismissed(Snackbar snackbar, int event) {
                        // Only delete/navigate if the Snackbar is dismissed without UNDO
                        if (event != Snackbar.Callback.DISMISS_EVENT_ACTION) {
                            if (direction == ItemTouchHelper.LEFT) {
                                // Check if the item was actually removed (not undone)
                                if (!adapter.getExpenseList().contains(expenseItem)) {
                                    deleteExpense(expenseItem, position);
                                }
                            } else {
                                // Navigate to detail view on right swipe dismissal (if not undone)
                                if (adapter.getExpenseList().contains(expenseItem)) {
                                    navigateToExpenseDetail(expenseItem.getId());
                                } else {
                                    // If undone, the item is back, no need to navigate again
                                    // Or handle as needed, maybe refresh list
                                }
                            }
                        } else {
                            // If UNDO was clicked, ensure the item is visually restored correctly
                            adapter.notifyItemChanged(position);
                        }
                        // Reset item view properties after swipe/dismissal
                        RecyclerView.ViewHolder currentViewHolder = recyclerView
                                .findViewHolderForAdapterPosition(position);
                        if (currentViewHolder != null) {
                            currentViewHolder.itemView.setScaleY(1f);
                            currentViewHolder.itemView.setRotation(0f);
                        }
                    }
                });

                snackbar.show();
            }
        };

        new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(recyclerView);
    }

    private void navigateToExpenseDetail(String expenseId) {
        // Navigate to ExpenseDetailFragment instead of Edit
        ExpenseDetailFragment detailFragment = new ExpenseDetailFragment();
        Bundle args = new Bundle();
        args.putString("expense_id", expenseId);
        detailFragment.setArguments(args);

        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, detailFragment) // Use the correct container ID
                .addToBackStack(null)
                .commit();
    }

    private void loadExpenses() {
        if (mAuth.getCurrentUser() == null) {
            Log.e(TAG, "User not logged in.");
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        tvEmptyState.setVisibility(View.GONE);

        String dbGuid = GuidUtils.getUserDbGuid(requireContext());
        ExpenseApi expenseApi = RetrofitClient.getClient().create(ExpenseApi.class);
        Call<List<Expense>> call = expenseApi.getExpenses(dbGuid);

        call.enqueue(new Callback<List<Expense>>() {
            @Override
            public void onResponse(@NonNull Call<List<Expense>> call, @NonNull Response<List<Expense>> response) {
                progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    expenses.clear();
                    expenses.addAll(response.body());
                    adapter.setExpenses(expenses);

                    // Show empty state if no expenses
                    if (expenses.isEmpty()) {
                        tvEmptyState.setVisibility(View.VISIBLE);
                    } else {
                        tvEmptyState.setVisibility(View.GONE);
                    }
                } else {
                    Log.e(TAG, "Failed to load expenses. Code: " + response.code());
                    Snackbar.make(recyclerView, "Failed to load expenses", Snackbar.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Expense>> call, @NonNull Throwable t) {
                progressBar.setVisibility(View.GONE);
                Log.e(TAG, "Error loading expenses", t);
                Snackbar.make(recyclerView, "Error loading expenses: " + t.getMessage(), Snackbar.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteExpense(Expense expense, int position) {
        // Ensure expense and ID are not null before proceeding
        if (expense == null || expense.getId() == null) {
            Log.e(TAG, "Cannot delete null expense or expense with null ID.");
            // Optionally restore item visually if deletion fails early
            // expenses.add(position, expense); // Be careful with null expense here
            // adapter.setExpenses(expenses);
            Snackbar.make(recyclerView, "Error: Invalid expense data", Snackbar.LENGTH_SHORT).show();
            return;
        }

        String dbGuid = GuidUtils.getUserDbGuid(requireContext());
        ExpenseApi expenseApi = RetrofitClient.getClient().create(ExpenseApi.class);
        Call<Void> call = expenseApi.deleteExpense(dbGuid, expense.getId());

        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (!response.isSuccessful()) {
                    Log.e(TAG, "Failed to delete expense from backend. Code: " + response.code() + ", ID: "
                            + expense.getId());
                    // Restore item in the main list and update adapter
                    if (!expenses.contains(expense)) {
                        if (position <= expenses.size()) {
                            expenses.add(position, expense);
                        } else {
                            expenses.add(expense);
                        }
                        adapter.setExpenses(new ArrayList<>(expenses)); // Refresh adapter list
                        adapter.notifyItemInserted(position);
                    }
                    Snackbar.make(recyclerView, "Failed to delete expense", Snackbar.LENGTH_SHORT).show();
                } else {
                    Log.d(TAG, "Expense deleted successfully from backend. ID: " + expense.getId());
                    // Remove from the main list as well if backend deletion was successful
                    expenses.remove(expense);
                    // No need to notify adapter again, it was removed optimistically
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                Log.e(TAG, "Error deleting expense from backend. ID: " + expense.getId(), t);
                // Restore item in the main list and update adapter
                if (!expenses.contains(expense)) {
                    if (position <= expenses.size()) {
                        expenses.add(position, expense);
                    } else {
                        expenses.add(expense);
                    }
                    adapter.setExpenses(new ArrayList<>(expenses)); // Refresh adapter list
                    adapter.notifyItemInserted(position);
                }
                Snackbar.make(recyclerView, "Error deleting expense: " + t.getMessage(), Snackbar.LENGTH_SHORT).show();
            }
        });
    }

    // OnExpenseClickListener implementation
    @Override
    public void onExpenseClick(Expense expense, int position) {
        navigateToExpenseDetail(expense.getId());
    }

    @Override
    public void onDeleteClick(Expense expense, int position) {
        // Show confirmation dialog
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(
                requireContext());
        builder.setTitle("Confirm Delete");
        builder.setMessage("Are you sure you want to delete this expense?");
        builder.setPositiveButton("Delete", (dialog, which) -> {
            expenses.remove(position);
            adapter.setExpenses(expenses);
            deleteExpense(expense, position);
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    @Override
    public void onIncreaseQuantity(Expense expense, int position) {
        int quantity = expense.getQuantity() != null ? expense.getQuantity() : 1;
        expense.setQuantity(quantity + 1);
        adapter.notifyItemChanged(position);
        updateExpense(expense);
    }

    @Override
    public void onDecreaseQuantity(Expense expense, int position) {
        int quantity = expense.getQuantity() != null ? expense.getQuantity() : 1;
        if (quantity > 1) {
            expense.setQuantity(quantity - 1);
            adapter.notifyItemChanged(position);
            updateExpense(expense);
        }
    }

    private void updateExpense(Expense expense) {
        String dbGuid = GuidUtils.getUserDbGuid(requireContext());
        ExpenseApi expenseApi = RetrofitClient.getClient().create(ExpenseApi.class);
        Call<Expense> call = expenseApi.updateExpense(dbGuid, expense.getId(), expense);

        call.enqueue(new Callback<Expense>() {
            @Override
            public void onResponse(Call<Expense> call, Response<Expense> response) {
                if (!response.isSuccessful()) {
                    Log.e(TAG, "Failed to update expense. Code: " + response.code());
                    Snackbar.make(recyclerView, "Failed to update expense", Snackbar.LENGTH_SHORT).show();
                    loadExpenses(); // Reload to get the correct data
                }
            }

            @Override
            public void onFailure(Call<Expense> call, Throwable t) {
                Log.e(TAG, "Error updating expense", t);
                Snackbar.make(recyclerView, "Error updating expense: " + t.getMessage(), Snackbar.LENGTH_SHORT).show();
                loadExpenses(); // Reload to get the correct data
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        // Reload expenses when fragment resumes
        loadExpenses();
    }
}
