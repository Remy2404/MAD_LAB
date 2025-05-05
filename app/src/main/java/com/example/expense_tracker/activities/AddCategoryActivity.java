package com.example.expense_tracker.activities;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.example.expense_tracker.R;
import com.example.expense_tracker.db.AppDatabase;
import com.example.expense_tracker.db.CategoryEntity;

import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AddCategoryActivity extends BaseActivity {

    private EditText etCategoryName;
    private Button btnAddCategory, btnCancel;
    private ExecutorService executorService;
    private static final String TAG = "AddCategoryActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_category);

        // Initialize ExecutorService for background operations
        executorService = Executors.newSingleThreadExecutor();

        // Initialize views
        etCategoryName = findViewById(R.id.etCategoryName);
        btnAddCategory = findViewById(R.id.btnAddCategory);
        btnCancel = findViewById(R.id.btnCancel);

        // Setup click listeners
        btnAddCategory.setOnClickListener(v -> addCategory());
        btnCancel.setOnClickListener(v -> finish());
    }

    private void addCategory() {
        String categoryName = etCategoryName.getText().toString().trim();

        // Validate input
        if (TextUtils.isEmpty(categoryName)) {
            etCategoryName.setError("Category name is required");
            return;
        }

        // Create a new category entity
        CategoryEntity category = new CategoryEntity(UUID.randomUUID().toString(), categoryName);

        // Insert the category into RoomDB using ExecutorService
        executorService.execute(() -> {
            try {
                AppDatabase.getInstance(this).categoryDao().insert(category);
                
                // Update UI on the main thread
                runOnUiThread(() -> {
                    Toast.makeText(this, "Category added successfully", Toast.LENGTH_SHORT).show();
                    finish(); // Close the activity and go back
                });
            } catch (Exception e) {
                Log.e(TAG, "Error adding category", e);
                
                // Show error on the main thread
                runOnUiThread(() -> {
                    Toast.makeText(this, "Error adding category: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Shutdown ExecutorService when activity is destroyed
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        hideSystemUI();
    }

    private void hideSystemUI() {
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
    }
}