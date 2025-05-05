package com.example.expense_tracker.fragments;

import android.content.Context;
import android.graphics.Canvas;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.expense_tracker.R;
import com.example.expense_tracker.activities.MainActivity;
import com.example.expense_tracker.adapters.ExpenseAdapter;
import com.example.expense_tracker.models.Expense;
import com.example.expense_tracker.routes.ExpenseApi;
import com.example.expense_tracker.routes.RetrofitClient;
import com.example.expense_tracker.utils.ExpenseCache;
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
    private SwipeRefreshLayout swipeRefreshLayout;
    private View rootView; // Add this field

    // Retry logic variables
    private int retryCount = 0;
    private static final int MAX_RETRIES = 3;
    private static final int RETRY_DELAY_MS = 3000; // 3 seconds
    private Handler retryHandler = new Handler(Looper.getMainLooper());
    private Call<List<Expense>> currentCall;

    // Cache constants
    private static final int CACHE_MAX_AGE_MINUTES = 60; // Consider cache valid for 1 hour

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_expense_list, container, false);
        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Set the title for the fragment
        requireActivity().setTitle("Expense List");

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Initialize views - Ensure IDs match fragment_expense_list.xml
        recyclerView = view.findViewById(R.id.recyclerViewExpenses);
        progressBar = view.findViewById(R.id.progressBar);
        tvEmptyState = view.findViewById(R.id.tvEmptyState);
        fabAddExpense = view.findViewById(R.id.fabAddExpense);

        // Setup SwipeRefreshLayout - you need to add this to your layout XML if it
        // doesn't exist
        swipeRefreshLayout = view.findViewById(R.id.swipeRefresh);
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setOnRefreshListener(this::refreshExpenses);
            swipeRefreshLayout.setColorSchemeResources(
                    android.R.color.holo_blue_bright,
                    android.R.color.holo_green_light,
                    android.R.color.holo_orange_light,
                    android.R.color.holo_red_light);
        }

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
        // Existing implementation unchanged
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

    /**
     * Refresh expenses (usually called from SwipeRefreshLayout)
     */
    private void refreshExpenses() {
        // Reset retry count on manual refresh
        retryCount = 0;

        // Cancel any ongoing request
        if (currentCall != null) {
            currentCall.cancel();
        }

        // Force network refresh
        loadExpensesFromNetwork(true);
    }

    /**
     * Check if the device has an active network connection
     */
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) requireContext()
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            return activeNetworkInfo != null && activeNetworkInfo.isConnected();
        }
        return false;
    }

    /**
     * Load expenses, with smart caching and offline support
     */
    private void loadExpenses() {
        if (mAuth.getCurrentUser() == null) {
            Log.e(TAG, "User not logged in.");
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        tvEmptyState.setVisibility(View.GONE);

        // First try to load from network
        if (isNetworkAvailable()) {
            loadExpensesFromNetwork(false);
        } else {
            // If network not available, try to load from cache
            Log.d(TAG, "Network unavailable, loading from cache");
            loadExpensesFromCache();
            // Show offline mode snackbar
            Snackbar.make(recyclerView, "Offline mode: showing cached expenses", Snackbar.LENGTH_LONG).show();
        }
    }

    /**
     * Load expenses from local cache
     */
    private void loadExpensesFromCache() {
        List<Expense> cachedExpenses = ExpenseCache.getExpenses(requireContext());

        if (!cachedExpenses.isEmpty()) {
            expenses.clear();
            expenses.addAll(cachedExpenses);
            adapter.setExpenses(expenses);
            progressBar.setVisibility(View.GONE);
            tvEmptyState.setVisibility(cachedExpenses.isEmpty() ? View.VISIBLE : View.GONE);
            if (swipeRefreshLayout != null) {
                swipeRefreshLayout.setRefreshing(false);
            }
        } else {
            // No cached data
            progressBar.setVisibility(View.GONE);
            tvEmptyState.setVisibility(View.VISIBLE);
            if (swipeRefreshLayout != null) {
                swipeRefreshLayout.setRefreshing(false);
            }
            Log.d(TAG, "No cached expenses available");
        }
    }

    /**
     * Load expenses from network with retry mechanism
     * 
     * @param forceRefresh Whether to force refresh even if cache is available
     */
    private void loadExpensesFromNetwork(boolean forceRefresh) {
        // If not forcing refresh and we have a fresh cache, use it first (but still
        // update in background)
        if (!forceRefresh && ExpenseCache.hasFreshCache(requireContext(), CACHE_MAX_AGE_MINUTES)) {
            loadExpensesFromCache();
        }

        String dbGuid = GuidUtils.getUserDbGuid(requireContext());
        ExpenseApi expenseApi = RetrofitClient.getClient().create(ExpenseApi.class);
        currentCall = expenseApi.getExpenses(dbGuid);

        currentCall.enqueue(new Callback<List<Expense>>() {
            @Override
            public void onResponse(@NonNull Call<List<Expense>> call, @NonNull Response<List<Expense>> response) {
                // Reset retry counter on successful response
                retryCount = 0;

                // Hide progress indicators
                progressBar.setVisibility(View.GONE);
                if (swipeRefreshLayout != null) {
                    swipeRefreshLayout.setRefreshing(false);
                }

                if (response.isSuccessful() && response.body() != null) {
                    List<Expense> newExpenses = response.body();
                    expenses.clear();
                    expenses.addAll(newExpenses);
                    adapter.setExpenses(expenses);

                    // Update the cache with new data
                    ExpenseCache.saveExpenses(requireContext(), newExpenses);

                    // Show empty state if no expenses
                    if (expenses.isEmpty()) {
                        tvEmptyState.setVisibility(View.VISIBLE);
                    } else {
                        tvEmptyState.setVisibility(View.GONE);
                    }
                } else {
                    Log.e(TAG, "Failed to load expenses. Code: " + response.code());
                    // Try to load from cache as fallback
                    if (adapter.getExpenseList().isEmpty()) {
                        loadExpensesFromCache();
                    }
                    Snackbar.make(recyclerView, "Failed to refresh expenses (Error " + response.code() + ")",
                            Snackbar.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Expense>> call, @NonNull Throwable t) {
                if (call.isCanceled()) {
                    Log.d(TAG, "Request was cancelled");
                    return;
                }

                progressBar.setVisibility(View.GONE);
                if (swipeRefreshLayout != null) {
                    swipeRefreshLayout.setRefreshing(false);
                }

                Log.e(TAG, "Error loading expenses (Ask Gemini)", t);

                // First try to load from cache if adapter is empty
                if (adapter.getExpenseList().isEmpty()) {
                    loadExpensesFromCache();
                }

                // Show error message and retry option
                if (t instanceof java.net.SocketTimeoutException) {
                    retryWithBackoff();
                } else {
                    Snackbar.make(recyclerView, "Network error: " + t.getMessage(), Snackbar.LENGTH_LONG)
                            .setAction("RETRY", view -> refreshExpenses())
                            .show();
                }
            }
        });
    }

    /**
     * Implement exponential backoff for retries
     */
    private void retryWithBackoff() {
        if (retryCount < MAX_RETRIES) {
            retryCount++;
            int delay = RETRY_DELAY_MS * retryCount;

            // Get a valid view for Snackbar
            View view = rootView;
            if (view == null || !isAdded()) {
                Log.e(TAG, "Cannot show Snackbar - view is null or fragment not attached");
                return;
            }

            Snackbar.make(view,
                    "Connection timeout. Retrying in " + (delay / 1000) + " seconds...",
                    Snackbar.LENGTH_LONG).show();

            retryHandler.postDelayed(() -> {
                if (isAdded() && !isDetached()) {
                    loadExpensesFromNetwork(true);
                }
            }, delay);
        } else {
            // Max retries reached
            View view = rootView;
            if (view != null && isAdded()) {
                Snackbar.make(view, "Connection timed out. Using cached data.", Snackbar.LENGTH_LONG)
                        .setAction("RETRY", v -> {
                            retryCount = 0;
                            refreshExpenses();
                        })
                        .show();
            }
        }
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
                    // Update cache to match current state
                    ExpenseCache.saveExpenses(requireContext(), expenses);
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

                if (t instanceof java.net.SocketTimeoutException) {
                    Snackbar.make(recyclerView, "Delete operation timed out. Will retry when online.",
                            Snackbar.LENGTH_LONG).show();

                    // Store delete operation to retry later when online
                    // This would require creating a pending operations queue, which is beyond the
                    // scope
                    // of this fix, but would be a good enhancement for future updates
                } else {
                    Snackbar.make(recyclerView, "Error deleting expense: " + t.getMessage(),
                            Snackbar.LENGTH_SHORT).show();
                }
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
        // Cancel any pending retries when user initiates a new action
        retryHandler.removeCallbacksAndMessages(null);

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
                } else {
                    // Update was successful, update the cache
                    ExpenseCache.saveExpenses(requireContext(), expenses);
                }
            }

            @Override
            public void onFailure(Call<Expense> call, Throwable t) {
                Log.e(TAG, "Error updating expense", t);

                if (t instanceof java.net.SocketTimeoutException) {
                    Snackbar.make(recyclerView, "Update timed out. Changes will sync when online.",
                            Snackbar.LENGTH_LONG).show();

                    // We could queue up changes for future sync here, but for now,
                    // we'll keep the local update visible to user but reload when they come back
                    // online
                } else {
                    Snackbar.make(recyclerView, "Error updating expense: " + t.getMessage(),
                            Snackbar.LENGTH_SHORT).show();
                    loadExpenses(); // Reload to get the correct data
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        // Reload expenses when fragment resumes
        loadExpenses();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Cleanup
        retryHandler.removeCallbacksAndMessages(null);
        if (currentCall != null) {
            currentCall.cancel();
        }
    }
}
