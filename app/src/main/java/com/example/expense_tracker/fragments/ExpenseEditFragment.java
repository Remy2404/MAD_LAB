package com.example.expense_tracker.fragments;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.expense_tracker.R;
import com.example.expense_tracker.models.Expense;
import com.example.expense_tracker.routes.ExpenseApi;
import com.example.expense_tracker.routes.RetrofitClient;
import com.example.expense_tracker.utils.GuidUtils;
import com.example.expense_tracker.utils.ImageStorageHelper;
import com.example.expense_tracker.utils.SupabaseHelper;
import com.google.firebase.auth.FirebaseAuth;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ExpenseEditFragment extends Fragment {
    private static final String TAG = "ExpenseEditFragment";
    private static final int REQUEST_CAMERA_PERMISSION = 100;
    private static final int REQUEST_GALLERY_PERMISSION = 101;

    // UI Components
    private EditText titleEditText, amountEditText, remarkEditText;
    private Spinner currencySpinner, categorySpinner;
    private Button saveButton, cancelButton, takePhotoButton, chooseGalleryButton, removeImageButton;
    private ProgressBar progressBar;
    private CardView receiptImageCardView;
    private ImageView receiptImageView;

    // Data
    private String expenseId;
    private FirebaseAuth mAuth;
    private Uri currentImageUri = null;
    private String receiptImageUrl = null;
    private File photoFile = null;

    // Activity Result Launchers
    private ActivityResultLauncher<Intent> cameraLauncher;
    private ActivityResultLauncher<Intent> galleryLauncher;
    private ActivityResultLauncher<String[]> requestPermissionLauncher;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        // Initialize activity result launchers
        registerActivityResultLaunchers();

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_expense_edit, container, false);
    }

    private void registerActivityResultLaunchers() {
        // Permission launcher
        requestPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(),
                permissions -> {
                    boolean cameraGranted = Boolean.TRUE.equals(
                            permissions.get(Manifest.permission.CAMERA));

                    if (cameraGranted) {
                        openCamera();
                    } else {
                        Toast.makeText(getContext(),
                                "Camera permission is required to take photos",
                                Toast.LENGTH_SHORT).show();
                    }
                });

        // Camera launcher
        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        if (photoFile != null && photoFile.exists()) {
                            handleImageCapture(Uri.fromFile(photoFile));
                        }
                    }
                });

        // Gallery launcher
        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri selectedImageUri = result.getData().getData();
                        if (selectedImageUri != null) {
                            handleImageCapture(selectedImageUri);
                        }
                    }
                });
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Set the title for the fragment
        requireActivity().setTitle("Edit Expense");

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Initialize Supabase
        SupabaseHelper.initialize();

        // Initialize views
        initializeViews(view);

        // Setup spinners
        setupSpinners();

        // Get expense data from arguments
        if (getArguments() != null) {
            expenseId = getArguments().getString("expense_id");
            populateFields();
        } else {
            Toast.makeText(getContext(), "No expense data provided", Toast.LENGTH_SHORT).show();
            requireActivity().onBackPressed();
        }

        // Set up button click listeners
        setupClickListeners();
    }

    private void initializeViews(View view) {
        titleEditText = view.findViewById(R.id.editTextTitle);
        amountEditText = view.findViewById(R.id.editTextAmount);
        remarkEditText = view.findViewById(R.id.editTextRemark);
        currencySpinner = view.findViewById(R.id.currencySpinner);
        categorySpinner = view.findViewById(R.id.categorySpinner);
        saveButton = view.findViewById(R.id.buttonSave);
        cancelButton = view.findViewById(R.id.buttonCancel);
        progressBar = view.findViewById(R.id.progressBar); // Receipt image views
        takePhotoButton = view.findViewById(R.id.buttonTakePhoto);
        chooseGalleryButton = view.findViewById(R.id.buttonChooseGallery);
        receiptImageCardView = view.findViewById(R.id.receiptImageCardView);
        receiptImageView = view.findViewById(R.id.receiptImageView);
        removeImageButton = view.findViewById(R.id.buttonRemoveImage);
    }

    private void setupSpinners() {
        // Setup currency spinner
        ArrayAdapter<CharSequence> currencyAdapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.currency_options,
                android.R.layout.simple_spinner_item);
        currencyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        currencySpinner.setAdapter(currencyAdapter);

        // Setup category spinner
        ArrayAdapter<CharSequence> categoryAdapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.category_options,
                android.R.layout.simple_spinner_item);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(categoryAdapter);
    }

    private void populateFields() {
        if (getArguments() == null)
            return;

        // Set fields with data from arguments
        String title = getArguments().getString("title");
        double amount = getArguments().getDouble("amount");
        String currency = getArguments().getString("currency");
        String category = getArguments().getString("category");
        String remark = getArguments().getString("remark");
        receiptImageUrl = getArguments().getString("receiptImageUrl");

        // Populate the EditText fields
        if (!TextUtils.isEmpty(title)) {
            titleEditText.setText(title);
        }

        amountEditText.setText(String.valueOf(amount));

        if (!TextUtils.isEmpty(remark)) {
            remarkEditText.setText(remark);
        }

        // Set spinner selections
        setSpinnerSelection(currencySpinner, currency);
        setSpinnerSelection(categorySpinner, category);

        // Load receipt image if it exists
        if (!TextUtils.isEmpty(receiptImageUrl)) {
            receiptImageCardView.setVisibility(View.VISIBLE);
            Glide.with(this)
                    .load(receiptImageUrl)
                    .centerCrop()
                    .into(receiptImageView);
        }
    }

    private void setSpinnerSelection(Spinner spinner, String value) {
        if (value == null || value.isEmpty())
            return;

        ArrayAdapter adapter = (ArrayAdapter) spinner.getAdapter();
        for (int i = 0; i < adapter.getCount(); i++) {
            if (adapter.getItem(i).toString().equals(value)) {
                spinner.setSelection(i);
                break;
            }
        }
    }

    private void setupClickListeners() {
        saveButton.setOnClickListener(v -> saveExpense());
        cancelButton.setOnClickListener(v -> requireActivity().onBackPressed());

        // Receipt image buttons
        takePhotoButton.setOnClickListener(v -> checkCameraPermissionAndOpen());
        chooseGalleryButton.setOnClickListener(v -> openGallery());
        removeImageButton.setOnClickListener(v -> removeImage());
    }

    private void checkCameraPermissionAndOpen() {
        if (ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(new String[] { Manifest.permission.CAMERA });
        } else {
            openCamera();
        }
    }

    private void openCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        // Create the file where the photo should go
        try {
            photoFile = createImageFile();
        } catch (IOException ex) {
            Log.e(TAG, "Error creating image file", ex);
            Toast.makeText(getContext(), "Error creating image file", Toast.LENGTH_SHORT).show();
            return;
        }

        // Continue only if the file was successfully created
        if (photoFile != null) {
            Uri photoURI = FileProvider.getUriForFile(requireContext(),
                    "com.example.expense_tracker.fileprovider",
                    photoFile);
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
            cameraLauncher.launch(takePictureIntent);
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(
                imageFileName, /* prefix */
                ".jpg", /* suffix */
                storageDir /* directory */
        );
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryLauncher.launch(intent);
    }

    private void handleImageCapture(Uri imageUri) {
        if (imageUri != null) {
            currentImageUri = imageUri;

            // Show the image preview
            receiptImageCardView.setVisibility(View.VISIBLE);
            Glide.with(this)
                    .load(imageUri)
                    .centerCrop()
                    .into(receiptImageView);
        }
    }

    private void removeImage() {
        currentImageUri = null;
        receiptImageUrl = null;
        receiptImageCardView.setVisibility(View.GONE);

        // Delete the photo file if it exists
        if (photoFile != null && photoFile.exists()) {
            photoFile.delete();
            photoFile = null;
        }
    }

    private void saveExpense() {
        // Validate input
        if (!validateInput()) {
            return;
        }

        // Show progress
        progressBar.setVisibility(View.VISIBLE);

        // Get the data from form fields
        String title = titleEditText.getText().toString().trim();
        double amount;
        try {
            amount = Double.parseDouble(amountEditText.getText().toString().trim());
        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "Invalid amount format", Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.GONE);
            return;
        }

        String currency = currencySpinner.getSelectedItem().toString();
        String category = categorySpinner.getSelectedItem().toString();
        String remark = remarkEditText.getText().toString().trim();

        // Get database GUID
        String dbGuid = GuidUtils.getUserDbGuid(requireContext());
        // If we have a new image to upload
        if (currentImageUri != null && receiptImageUrl == null) {
            // Upload image using the SupabaseHelper with fallback to ImageStorageHelper
            CompletableFuture<String> uploadFuture = SupabaseHelper.uploadImageWithFallback(
                    requireContext(), currentImageUri);

            uploadFuture.whenComplete((url, throwable) -> {
                if (throwable != null) {
                    requireActivity().runOnUiThread(() -> {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(getContext(),
                                "Error uploading image: " + throwable.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    });
                } else {
                    // Save the image URL
                    receiptImageUrl = url;

                    // Proceed with updating expense
                    requireActivity().runOnUiThread(() -> updateExpenseInDatabase(
                            dbGuid, title, amount, currency, category, remark, receiptImageUrl));
                }
            });
        } else {
            // No new image, update expense directly
            updateExpenseInDatabase(dbGuid, title, amount, currency, category, remark, receiptImageUrl);
        }
    }

    private void updateExpenseInDatabase(String dbGuid, String title, double amount,
            String currency, String category, String remark, String imageUrl) {
        // Create expense object for update
        Expense updatedExpense = new Expense();
        updatedExpense.setId(expenseId);
        updatedExpense.setTitle(title);
        updatedExpense.setAmount(amount);
        updatedExpense.setCurrency(currency);
        updatedExpense.setCategory(category);
        updatedExpense.setRemark(remark);
        updatedExpense.setReceiptImageUrl(imageUrl);

        // Call API to update expense
        ExpenseApi expenseApi = RetrofitClient.getClient().create(ExpenseApi.class);
        Call<Expense> call = expenseApi.updateExpense(dbGuid, expenseId, updatedExpense);

        call.enqueue(new Callback<Expense>() {
            @Override
            public void onResponse(Call<Expense> call, Response<Expense> response) {
                progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "Expense updated successfully: " + response.body().getId());
                    Toast.makeText(getContext(), "Expense updated successfully", Toast.LENGTH_SHORT).show();

                    // Return to previous screen
                    requireActivity().onBackPressed();
                } else {
                    Log.e(TAG, "Failed to update expense. Code: " + response.code());
                    try {
                        if (response.errorBody() != null) {
                            Log.e(TAG, "Error body: " + response.errorBody().string());
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing error response", e);
                    }

                    Toast.makeText(getContext(), "Failed to update expense", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Expense> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Log.e(TAG, "Error updating expense", t);
                Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean validateInput() {
        boolean isValid = true;

        // Validate amount (required)
        String amountStr = amountEditText.getText().toString().trim();
        if (TextUtils.isEmpty(amountStr)) {
            amountEditText.setError("Amount is required");
            isValid = false;
        } else {
            try {
                double amount = Double.parseDouble(amountStr);
                if (amount <= 0) {
                    amountEditText.setError("Amount must be greater than 0");
                    isValid = false;
                }
            } catch (NumberFormatException e) {
                amountEditText.setError("Invalid amount format");
                isValid = false;
            }
        }

        // Either title or category must be provided
        String title = titleEditText.getText().toString().trim();
        String category = categorySpinner.getSelectedItem().toString();

        if (TextUtils.isEmpty(title) && (TextUtils.isEmpty(category) || category.equals("Select Category"))) {
            titleEditText.setError("Either title or category must be provided");
            isValid = false;
        }

        return isValid;
    }
}
