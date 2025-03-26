package com.example.expense_tracker.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.expense_tracker.R;
import com.example.expense_tracker.fragments.AddExpense_Fragment;
import com.example.expense_tracker.fragments.ExpenseDetailFragment;
import com.example.expense_tracker.fragments.Expense_ListFragment;
import com.example.expense_tracker.fragments.HomeFragment;
import com.example.expense_tracker.routes.RetrofitClient;
import com.example.expense_tracker.utils.GuidUtils;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        try {
            // Remove toolbar setup - it's causing the conflict
            
            mAuth = FirebaseAuth.getInstance();

            // Check if user is logged in
            if (mAuth.getCurrentUser() == null) {
                // User is not logged in, redirect to login
                startActivity(new Intent(this, LoginActivity.class));
                finish();
                return;
            }

            // Set the user GUID as the database name for Retrofit using our utility class
            String dbGuid = GuidUtils.getUserDbGuid(this);
            Log.d(TAG, "Using GUID for API calls: " + dbGuid);
            RetrofitClient.setDbName(dbGuid);

            // Setup bottom navigation
            BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNav);
            if (bottomNavigationView == null) {
                Log.e(TAG, "bottomNav view not found in layout");
                Toast.makeText(this, "Layout error: Navigation not found", Toast.LENGTH_SHORT).show();
                return;
            }

            bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
                try {
                    int itemId = item.getItemId();
                    
                    if (itemId == R.id.nav_home) {
                        loadFragment(new HomeFragment(), "HomeFragment");
                        return true;
                    } else if (itemId == R.id.nav_add_expense) {
                        loadFragment(new AddExpense_Fragment(), "AddExpense_Fragment");
                        return true;
                    } else if (itemId == R.id.nav_expense_list) {
                        loadFragment(new Expense_ListFragment(), "Expense_ListFragment");
                        return true;
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error loading fragment: " + e.getMessage());
                    Toast.makeText(this, "Error loading screen: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
                
                return false;
            });

            // Set default fragment
            if (savedInstanceState == null) {
                try {
                    loadFragment(new HomeFragment(), "HomeFragment");
                } catch (Exception e) {
                    Log.e(TAG, "Error loading initial fragment: " + e.getMessage());
                    Toast.makeText(this, "Error loading initial screen: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate: " + e.getMessage());
            Toast.makeText(this, "Startup error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_sign_out) {
            mAuth.signOut();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Navigation methods for fragments
    public void navigateToHome() {
        loadFragment(new HomeFragment(), "HomeFragment");
    }

    public void navigateToExpenseList() {
        loadFragment(new Expense_ListFragment(), "Expense_ListFragment");
    }

    public void navigateToExpenseDetail(long expenseId) {
        try {
            ExpenseDetailFragment fragment = new ExpenseDetailFragment();
            Bundle args = new Bundle();
            args.putLong("expense_id", expenseId);
            fragment.setArguments(args);
            loadFragment(fragment, "ExpenseDetailFragment");
        } catch (Exception e) {
            Log.e(TAG, "Error navigating to expense detail: " + e.getMessage());
            Toast.makeText(this, "Error loading expense details", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadFragment(Fragment fragment, String tag) {
        if (isFinishing() || isDestroyed()) {
            return;
        }
        
        try {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, fragment, tag);
            if (!tag.equals("HomeFragment")) {
                transaction.addToBackStack(null);
            }
            transaction.commit();
        } catch (Exception e) {
            Log.e(TAG, "Error in loadFragment: " + e.getMessage());
            Toast.makeText(this, "Navigation error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}