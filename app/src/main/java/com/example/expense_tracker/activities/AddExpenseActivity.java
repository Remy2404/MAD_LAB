package com.example.expense_tracker.activities;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.expense_tracker.R;
import com.example.expense_tracker.models.Expense;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class AddExpenseActivity extends AppCompatActivity {

    private EditText etAmount;
    private EditText etRemark;
    private EditText etDate;
    private AutoCompleteTextView actvCurrency;
    private AutoCompleteTextView actvCategory;
    private ImageView ivReceiptPreview;
    private MaterialButton btnCaptureReceipt;
    private MaterialButton btnAddExpense;
    private CheckBox cbRecurring;

    private String currentPhotoPath;
    private Calendar selectedDate;
    private String[] currencies = {"USD", "EUR", "GBP", "JPY", "KHR"};
    private String[] categories = {"Food", "Transport", "Bills", "Entertainment", "Shopping", "Others"};

    private final ActivityResultLauncher<Intent> takePictureLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    // Display the captured image
                    displayCapturedImage();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_expense);

        // Initialize calendar with current date
        selectedDate = Calendar.getInstance();

        // Initialize views
        initializeViews();

        // Setup adapters for dropdown menus
        setupDropdowns();

        // Handle pre-selected category from quick add buttons
        handleQuickCategory();

        // Set current date
        updateDateDisplay();

        // Setup click listeners
        setupClickListeners();
    }

    private void initializeViews() {
        etAmount = findViewById(R.id.etAmount);
        actvCurrency = findViewById(R.id.actvCurrency);
        actvCategory = findViewById(R.id.actvCategory);
        etRemark = findViewById(R.id.etRemark);
        etDate = findViewById(R.id.etDate);
        ivReceiptPreview = findViewById(R.id.ivReceiptPreview);
        btnCaptureReceipt = findViewById(R.id.btnCaptureReceipt);
        btnAddExpense = findViewById(R.id.btnAddExpense);
        cbRecurring = findViewById(R.id.cbRecurring);
    }

    private void setupDropdowns() {
        // Setup currency dropdown
        ArrayAdapter<String> currencyAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_dropdown_item_1line, currencies);
        actvCurrency.setAdapter(currencyAdapter);
        actvCurrency.setText(currencies[0], false); // Default to first currency

        // Setup category dropdown
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_dropdown_item_1line, categories);
        actvCategory.setAdapter(categoryAdapter);
        actvCategory.setText(categories[0], false); // Default to first category
    }

    private void handleQuickCategory() {
        Intent intent = getIntent();
        if (intent.hasExtra("QUICK_CATEGORY")) {
            String quickCategory = intent.getStringExtra("QUICK_CATEGORY");
            actvCategory.setText(quickCategory, false);
        }

        // Check if we're editing an existing expense
        if (intent.hasExtra("EXPENSE_TO_EDIT")) {
            Expense expense = (Expense) intent.getSerializableExtra("EXPENSE_TO_EDIT");
            populateFieldsWithExpense(expense);
        }

        // Check if we're duplicating an expense
        if (intent.hasExtra("EXPENSE_TO_DUPLICATE")) {
            Expense expense = (Expense) intent.getSerializableExtra("EXPENSE_TO_DUPLICATE");
            populateFieldsWithExpense(expense);
            // Clear receipt since we want to capture a new one
            currentPhotoPath = null;
        }
    }

    private void populateFieldsWithExpense(Expense expense) {
        if (expense != null) {
            etAmount.setText(String.valueOf(expense.getAmount()));
            actvCurrency.setText(expense.getCurrency(), false);
            actvCategory.setText(expense.getCategory(), false);
            etRemark.setText(expense.getRemark());

            // Set the date
            try {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                Date expenseDate = dateFormat.parse(expense.getCreatedDate());
                if (expenseDate != null) {
                    selectedDate.setTime(expenseDate);
                    updateDateDisplay();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            // Set receipt path and display image if available
            currentPhotoPath = expense.getReceiptPath();
            if (currentPhotoPath != null && !currentPhotoPath.isEmpty()) {
                displayCapturedImage();
            }
        }
    }

    private void setupClickListeners() {
        // Setup date picker dialog
        etDate.setOnClickListener(v -> showDatePickerDialog());

        // Setup receipt capture
        btnCaptureReceipt.setOnClickListener(v -> captureReceipt());

        // Setup add button
        btnAddExpense.setOnClickListener(v -> saveExpense());
    }

    private void showDatePickerDialog() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    selectedDate.set(year, month, dayOfMonth);
                    updateDateDisplay();
                },
                selectedDate.get(Calendar.YEAR),
                selectedDate.get(Calendar.MONTH),
                selectedDate.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void updateDateDisplay() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMMM, yyyy", Locale.getDefault());
        etDate.setText(dateFormat.format(selectedDate.getTime()));
    }

    private void captureReceipt() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the file where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                Toast.makeText(this, "Error creating image file", Toast.LENGTH_SHORT).show();
            }

            // Continue only if the file was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(
                        this,
                        "com.example.expense_tracker.fileprovider",
                        photoFile
                );
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                takePictureLauncher.launch(takePictureIntent);
            }
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void displayCapturedImage() {
        if (currentPhotoPath != null) {
            // Scale the image to fit the ImageView
            int targetW = ivReceiptPreview.getWidth();
            int targetH = ivReceiptPreview.getHeight();

            // If dimensions are not yet available, use default values
            if (targetW == 0) targetW = 250;
            if (targetH == 0) targetH = 250;

            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            bmOptions.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(currentPhotoPath, bmOptions);

            int photoW = bmOptions.outWidth;
            int photoH = bmOptions.outHeight;

            // Calculate how much to scale down the image
            int scaleFactor = Math.max(1, Math.min(photoW/targetW, photoH/targetH));

            // Decode the image file into a Bitmap sized to fill the View
            bmOptions.inJustDecodeBounds = false;
            bmOptions.inSampleSize = scaleFactor;

            Bitmap bitmap = BitmapFactory.decodeFile(currentPhotoPath, bmOptions);
            ivReceiptPreview.setImageBitmap(bitmap);
        }
    }

    private void saveExpense() {
        // Validate fields
        if (etAmount.getText().toString().isEmpty()) {
            Toast.makeText(this, "Please enter an amount", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create expense object
        Expense expense = new Expense();
        expense.setAmount(Double.parseDouble(etAmount.getText().toString()));
        expense.setCurrency(actvCurrency.getText().toString());
        expense.setCategory(actvCategory.getText().toString());
        expense.setRemark(etRemark.getText().toString());

        // Format date for storage (YYYY-MM-DD)
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        expense.setCreatedDate(dateFormat.format(selectedDate.getTime()));

        // Set receipt path if available
        expense.setReceiptPath(currentPhotoPath);

        // Set recurring flag
        expense.setRecurring(cbRecurring.isChecked());

        // Return data to calling activity
        Intent resultIntent = new Intent();
        resultIntent.putExtra("EXPENSE_DATA", expense);
        setResult(RESULT_OK, resultIntent);
        finish();
    }
}