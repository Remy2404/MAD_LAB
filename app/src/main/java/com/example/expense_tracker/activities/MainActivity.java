package com.example.expense_tracker.activities;

            import static android.content.ContentValues.TAG;

            import androidx.activity.result.ActivityResult;
            import androidx.activity.result.ActivityResultCallback;
            import androidx.activity.result.ActivityResultLauncher;
            import androidx.activity.result.contract.ActivityResultContracts;
            import androidx.appcompat.app.AppCompatActivity;

            import android.content.Intent;
            import android.os.Bundle;
            import android.util.Log;
            import android.widget.Button;
            import android.widget.TextView;
            import android.widget.Toast;
            import android.widget.ProgressBar;

            import com.example.expense_tracker.R;
            import com.example.expense_tracker.models.Expense;
            import com.google.android.material.button.MaterialButton;

            import java.text.NumberFormat;
            import java.util.Locale;

            public class MainActivity extends AppCompatActivity {

                private TextView tvTitle;
                private TextView tvLastExpense;
                private TextView tvTotalAmount;
                private TextView tvBudgetLeft;
                private ProgressBar progressBudget;
                private MaterialButton btnAddExpense;
                private MaterialButton btnViewExpense;
                private Button btnQuickFood;
                private Button btnQuickTransport;
                private Button btnQuickBills;

                private Expense latestExpense;
                private double monthlyBudget = 1000.00;
                private double totalExpenses = 0.00;

                // Activity Result Launcher for Add Expense Activity
                private final ActivityResultLauncher<Intent> addExpenseLauncher = registerForActivityResult(
                        new ActivityResultContracts.StartActivityForResult(),
                        result -> {
                            if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                                // Get expense data from result
                                latestExpense = (Expense) result.getData().getSerializableExtra("EXPENSE_DATA");
                                if (latestExpense != null) {
                                    // Update total expenses
                                    totalExpenses += latestExpense.getAmount();
                                    // Update UI
                                    updateDashboardData();
                                    Toast.makeText(this, "Expense added successfully", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                );

                @Override
                protected void onCreate(Bundle savedInstanceState) {
                    super.onCreate(savedInstanceState);
                    setContentView(R.layout.activity_main);

                    // Initialize UI components
                    initializeUI();
                    setupClickListeners();
                    updateDashboardData();
                }

                private void initializeUI() {
                    tvTitle = findViewById(R.id.tvTitle);
                    tvLastExpense = findViewById(R.id.tvLastExpense);
                    tvTotalAmount = findViewById(R.id.tvTotalAmount);
                    tvBudgetLeft = findViewById(R.id.tvBudgetLeft);
                    progressBudget = findViewById(R.id.progressBudget);
                    btnAddExpense = findViewById(R.id.btnAddExpense);
                    btnViewExpense = findViewById(R.id.btnViewExpense);
                    btnQuickFood = findViewById(R.id.btnQuickFood);
                    btnQuickTransport = findViewById(R.id.btnQuickTransport);
                    btnQuickBills = findViewById(R.id.btnQuickBills);

                    tvTitle.setText("Manage Your Expense, Phon Ramy");
                }

                private void setupClickListeners() {
                    btnAddExpense.setOnClickListener(view -> {
                        try {
                            Intent intent = new Intent(MainActivity.this, AddExpenseActivity.class);
                            addExpenseLauncher.launch(intent);
                        } catch (Exception e) {
                            Log.e(TAG, "Error launching AddExpenseActivity: " + e.getMessage());
                            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

                    btnViewExpense.setOnClickListener(view -> {
                        try {
                            if (latestExpense != null) {
                                Intent intent = new Intent(MainActivity.this, ExpenseDetailActivity.class);
                                intent.putExtra("EXPENSE_DATA", latestExpense);
                                startActivity(intent);
                            } else {
                                Toast.makeText(this, "No expense to view. Add an expense first.", Toast.LENGTH_SHORT).show();
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error launching ExpenseDetailActivity: " + e.getMessage());
                            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

                    btnQuickFood.setOnClickListener(view -> {
                        try {
                            Intent intent = new Intent(MainActivity.this, AddExpenseActivity.class);
                            intent.putExtra("QUICK_CATEGORY", "Food");
                            addExpenseLauncher.launch(intent);
                        } catch (Exception e) {
                            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

                    btnQuickTransport.setOnClickListener(view -> {
                        try {
                            Intent intent = new Intent(MainActivity.this, AddExpenseActivity.class);
                            intent.putExtra("QUICK_CATEGORY", "Transport");
                            addExpenseLauncher.launch(intent);
                        } catch (Exception e) {
                            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

                    btnQuickBills.setOnClickListener(view -> {
                        try {
                            Intent intent = new Intent(MainActivity.this, AddExpenseActivity.class);
                            intent.putExtra("QUICK_CATEGORY", "Bills");
                            addExpenseLauncher.launch(intent);
                        } catch (Exception e) {
                            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                private void updateDashboardData() {
                    // Update last expense text
                    if (latestExpense != null) {
                        String expenseText = String.format(Locale.getDefault(),
                                "Last expense: %.2f %s for %s",
                                latestExpense.getAmount(),
                                latestExpense.getCurrency(),
                                latestExpense.getCategory());
                        tvLastExpense.setText(expenseText);
                    } else {
                        tvLastExpense.setText("No recent expenses");
                    }

                    // Format currency values
                    NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(Locale.US);

                    // Update total amount
                    tvTotalAmount.setText(currencyFormatter.format(totalExpenses));

                    // Calculate budget left
                    double budgetLeft = monthlyBudget - totalExpenses;
                    tvBudgetLeft.setText(currencyFormatter.format(budgetLeft));

                    // Update progress bar
                    int progressPercentage = (int)((totalExpenses / monthlyBudget) * 100);
                    progressBudget.setProgress(Math.min(progressPercentage, 100));
                }

                @Override
                protected void onResume() {
                    super.onResume();
                    updateDashboardData();
                }
            }