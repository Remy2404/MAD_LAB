package com.example.expense_tracker.fragments;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
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
import android.widget.TextView;
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
import androidx.lifecycle.LiveData;

import com.bumptech.glide.Glide;
import com.example.expense_tracker.R;
import com.example.expense_tracker.activities.AddCategoryActivity;
import com.example.expense_tracker.db.AppDatabase;
import com.example.expense_tracker.db.CategoryEntity;
import com.example.expense_tracker.models.Expense;
import com.example.expense_tracker.routes.ExpenseApi;
import com.example.expense_tracker.routes.RetrofitClient;
import com.example.expense_tracker.utils.GuidUtils;
import com.example.expense_tracker.utils.SupabaseConfig;
import com.example.expense_tracker.utils.SupabaseStorageUtils;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddExpense_Fragment extends Fragment {

    private EditText etAmount, etRemark, etTitle;
    private Spinner spinnerCurrency, spinnerCategory;
    private Button btnSave, btnCancel, btnAddCategory;
    private Button btnCaptureReceipt, btnGalleryReceipt;
    private ImageView ivReceiptPreview;
    private CardView cardReceiptPreview;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;
    private ArrayAdapter<String> categoryAdapter;
    private List<CategoryEntity> categoryList = new ArrayList<>();
    private ExecutorService executorService;
    private static final String TAG = "AddExpense_Fragment";

    // Camera and gallery related variables
    private ActivityResultLauncher<Intent> cameraLauncher;
    private ActivityResultLauncher<Intent> galleryLauncher;
    private ActivityResultLauncher<String> requestPermissionLauncher;
    private Uri currentPhotoUri; // URI for the photo captured by camera
    private Uri selectedImageUri; // URI of the selected image (from camera or gallery)
    private boolean hasImage = false; // Flag to track if an image is selected
    private String receiptImageUrl; // To store the uploaded image URL

    // Permission request code
    private static final int CAMERA_PERMISSION_REQUEST = 100;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_add_expense, container, false);

        // Register activity result launchers
        registerActivityResultLaunchers();

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Set the title for the fragment
        requireActivity().setTitle("Add Expense");

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Initialize ExecutorService for background operations
        executorService = Executors.newSingleThreadExecutor();

        // Initialize views
        etAmount = view.findViewById(R.id.etAmount);
        etRemark = view.findViewById(R.id.etRemark);
        etTitle = view.findViewById(R.id.etTitle);
        spinnerCurrency = view.findViewById(R.id.spinnerCurrency);
        spinnerCategory = view.findViewById(R.id.spinnerCategory);
        btnSave = view.findViewById(R.id.btnSave);
        btnCancel = view.findViewById(R.id.btnCancel);
        btnAddCategory = view.findViewById(R.id.btnAddCategory);

        // Initialize receipt image views
        btnCaptureReceipt = view.findViewById(R.id.btnCaptureReceipt);
        btnGalleryReceipt = view.findViewById(R.id.btnGalleryReceipt);
        ivReceiptPreview = view.findViewById(R.id.ivReceiptPreview);
        cardReceiptPreview = view.findViewById(R.id.cardReceiptPreview);
        progressBar = view.findViewById(R.id.addExpenseProgressBar);

        // Set up currency spinner
        ArrayAdapter<CharSequence> currencyAdapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.currency_options,
                android.R.layout.simple_spinner_item);
        currencyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCurrency.setAdapter(currencyAdapter);

        // Set up category spinner with an empty adapter initially
        categoryAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, new ArrayList<>());
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(categoryAdapter);

        // Load categories from RoomDB
        loadCategories();

        // Set up button click listeners
        btnSave.setOnClickListener(v -> saveExpense());
        btnCancel.setOnClickListener(v -> getParentFragmentManager().popBackStack());
        btnAddCategory.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), AddCategoryActivity.class);
            startActivity(intent);
        });

        // Image buttons click listeners
        btnCaptureReceipt.setOnClickListener(v -> {
            if (checkCameraPermission()) {
                dispatchTakePictureIntent();
            } else {
                requestCameraPermission();
            }
        });

        btnGalleryReceipt.setOnClickListener(v -> {
            Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            galleryLauncher.launch(galleryIntent);
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        // Reload categories when coming back from AddCategoryActivity
        loadCategories();
    }

    private void loadCategories() {
        executorService.execute(() -> {
            try {
                // Get categories from RoomDB
                List<CategoryEntity> dbCategories = AppDatabase.getInstance(requireContext())
                        .categoryDao()
                        .getAllCategories();

                // Update the categoryList
                categoryList.clear();
                categoryList.addAll(dbCategories);

                // Create a new list for the adapter
                List<String> categoryNames = new ArrayList<>();
                for (CategoryEntity category : categoryList) {
                    categoryNames.add(category.getName());
                }

                // Add default categories if the list is empty
                if (categoryNames.isEmpty()) {
                    String[] defaultCategories = requireContext().getResources()
                            .getStringArray(R.array.category_options);
                    for (String category : defaultCategories) {
                        categoryNames.add(category);
                        // Also save these default categories to RoomDB
                        CategoryEntity categoryEntity = new CategoryEntity(
                                UUID.randomUUID().toString(), category);
                        AppDatabase.getInstance(requireContext())
                                .categoryDao()
                                .insert(categoryEntity);
                    }
                }

                // Update the adapter on the main thread
                requireActivity().runOnUiThread(() -> {
                    categoryAdapter.clear();
                    categoryAdapter.addAll(categoryNames);
                    categoryAdapter.notifyDataSetChanged();
                });
            } catch (Exception e) {
                Log.e(TAG, "Error loading categories", e);
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(requireContext(),
                            "Error loading categories: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void saveExpense() {
        try {
            // Validate input
            String title = etTitle.getText().toString().trim();
            String amountStr = etAmount.getText().toString().trim();

            if (title.isEmpty()) {
                etTitle.setError("Title is required");
                return;
            }

            if (amountStr.isEmpty()) {
                etAmount.setError("Amount is required");
                return;
            }

            double amount = Double.parseDouble(amountStr);
            String currency = spinnerCurrency.getSelectedItem().toString();
            String category = spinnerCategory.getSelectedItem().toString();
            String remark = etRemark.getText().toString().trim();

            // Check if the user is logged in
            if (mAuth.getCurrentUser() == null) {
                Toast.makeText(getContext(), "User is not logged in", Toast.LENGTH_SHORT).show();
                return;
            }

            String userId = mAuth.getCurrentUser().getUid();

            // Show progress indicator
            if (progressBar != null) {
                progressBar.setVisibility(View.VISIBLE);
            } else {
                // Create an inline progress dialog
                Toast.makeText(getContext(), "Processing...", Toast.LENGTH_SHORT).show();
            }

            // Handle image upload if there's an image
            if (hasImage && selectedImageUri != null) {
                // Upload image to Supabase Storage
                SupabaseStorageUtils.uploadImage(requireContext(), selectedImageUri)
                        .thenAccept(imageUrl -> {
                            // Image upload success, now save expense with image URL
                            saveExpenseWithImageUrl(title, amount, currency, category, remark, userId, imageUrl);
                        })
                        .exceptionally(e -> {
                            // Image upload failed
                            Log.e(TAG, "Image upload failed", e);
                            if (isAdded()) {
                                requireActivity().runOnUiThread(() -> {
                                    if (progressBar != null) {
                                        progressBar.setVisibility(View.GONE);
                                    }
                                    Toast.makeText(getContext(),
                                            "Failed to upload image: " + e.getMessage(),
                                            Toast.LENGTH_SHORT).show();
                                });
                            }
                            return null;
                        });
            } else {
                // No image, save expense without imageUrl
                saveExpenseWithImageUrl(title, amount, currency, category, remark, userId, null);
            }
        } catch (NumberFormatException e) {
            etAmount.setError("Invalid amount");
        }
    }

    private void saveExpenseWithImageUrl(String title, double amount, String currency,
            String category, String remark, String userId, String imageUrl) {
        // Create expense object
        Expense expense = new Expense();
        expense.setId(UUID.randomUUID().toString());
        expense.setTitle(title);
        expense.setAmount(amount);
        expense.setCurrency(currency);
        expense.setCategory(category);
        expense.setRemark(remark);
        expense.setCreatedDate(new Date());
        expense.setCreatedBy(userId);

        // Set receipt image URL if available
        if (imageUrl != null && !imageUrl.isEmpty()) {
            expense.setReceiptImageUrl(imageUrl);
        }

        // Upload image to Supabase and save expense to API
        uploadImage(expense);
    }

    /**
     * Upload image to Supabase storage
     * 
     * @param expense The expense object to update with the image URL
     */
    private void uploadImage(Expense expense) {
        try {
            // Make sure Supabase is initialized
            if (!SupabaseConfig.isInitialized()) {
                SupabaseConfig.initialize(requireContext());
            }

            // Show status in the preview card
            cardReceiptPreview.setVisibility(View.VISIBLE);

            // Create a TextView for status if not already part of the layout
            TextView tvReceiptStatus = requireView().findViewById(R.id.tvReceiptStatus);
            if (tvReceiptStatus != null) {
                tvReceiptStatus.setText("Uploading receipt image...");
            }

            // Upload the image to Supabase
            SupabaseStorageUtils.uploadImage(requireContext(), selectedImageUri)
                    .thenAccept(imageUrl -> {
                        // Upload successful, update expense with image URL
                        Log.d(TAG, "Image uploaded successfully: " + imageUrl);
                        expense.setReceiptImageUrl(imageUrl);

                        if (tvReceiptStatus != null) {
                            tvReceiptStatus.setText("Receipt image uploaded");
                        }

                        // Now save the expense to the API
                        requireActivity().runOnUiThread(() -> saveExpenseToApi(expense));
                    })
                    .exceptionally(throwable -> {
                        // Handle upload error
                        Log.e(TAG, "Error uploading image", throwable);

                        requireActivity().runOnUiThread(() -> {
                            if (tvReceiptStatus != null) {
                                tvReceiptStatus.setText("Failed to upload receipt");
                            }

                            Toast.makeText(requireContext(),
                                    "Failed to upload receipt image: " + throwable.getMessage(),
                                    Toast.LENGTH_LONG).show();

                            progressBar.setVisibility(View.GONE);
                        });

                        return null;
                    });
        } catch (Exception e) {
            Log.e(TAG, "Error initiating image upload", e);
            Toast.makeText(requireContext(),
                    "Error uploading image: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
            progressBar.setVisibility(View.GONE);
        }
    }

    /**
     * Save expense to the API
     * 
     * @param expense The expense object to save
     */
    private void saveExpenseToApi(Expense expense) {
        // Get the GUID using our utility class
        String dbGuid = GuidUtils.getUserDbGuid(requireContext());

        // Send POST request to API
        ExpenseApi expenseAPI = RetrofitClient.getClient().create(ExpenseApi.class);
        Call<Expense> call = expenseAPI.createExpense(dbGuid, expense);

        call.enqueue(new Callback<Expense>() {
            @Override
            public void onResponse(Call<Expense> call, Response<Expense> response) {
                progressBar.setVisibility(View.GONE);

                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "Expense saved successfully", Toast.LENGTH_SHORT).show();
                    getParentFragmentManager().popBackStack();
                } else {
                    try {
                        String errorBody = response.errorBody().string();
                        Log.e("API Error", "Error code: " + response.code() + ", body: " + errorBody);
                        Toast.makeText(getContext(), "Failed to save expense: " + response.code(),
                                Toast.LENGTH_SHORT).show();
                    } catch (IOException e) {
                        Log.e("API Error", "Error parsing error response", e);
                        Toast.makeText(getContext(), "Failed to save expense", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<Expense> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Register activity result launchers
    private void registerActivityResultLaunchers() {
        // Camera launcher - handles the result of taking a picture
        cameraLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == Activity.RESULT_OK) {
                // Image captured successfully
                if (currentPhotoUri != null) {
                    selectedImageUri = currentPhotoUri;
                    hasImage = true;
                    displaySelectedImage();
                }
            } else {
                // User cancelled the camera operation
                Toast.makeText(requireContext(), "Image capture cancelled", Toast.LENGTH_SHORT).show();
            }
        });

        // Gallery launcher - handles the result of picking an image from gallery
        galleryLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                // Image selected from gallery
                selectedImageUri = result.getData().getData();
                if (selectedImageUri != null) {
                    hasImage = true;
                    displaySelectedImage();
                }
            } else {
                // User cancelled the gallery selection
                Toast.makeText(requireContext(), "Image selection cancelled", Toast.LENGTH_SHORT).show();
            }
        });

        // Permission launcher - handles the result of requesting camera permission
        requestPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        // Permission granted, take picture
                        dispatchTakePictureIntent();
                    } else {
                        // Permission denied
                        Toast.makeText(requireContext(),
                                "Camera permission is required to capture receipts",
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    /**
     * Check if camera permission is granted
     * 
     * @return true if camera permission is granted, false otherwise
     */
    private boolean checkCameraPermission() {
        return ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Request camera permission
     */
    private void requestCameraPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), Manifest.permission.CAMERA)) {
            // Show an explanation to the user
            new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                    .setTitle("Permission Required")
                    .setMessage("Camera permission is needed to capture receipt images")
                    .setPositiveButton("OK", (dialog, which) -> {
                        // Request the permission
                        requestPermissionLauncher.launch(Manifest.permission.CAMERA);
                    })
                    .setNegativeButton("Cancel", null)
                    .create()
                    .show();
        } else {
            // No explanation needed, request the permission
            requestPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    /**
     * Start camera intent to take a picture
     */
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(requireActivity().getPackageManager()) != null) {
            // Create the file where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
                Log.e(TAG, "Error creating image file", ex);
                Toast.makeText(requireContext(), "Error creating image file", Toast.LENGTH_SHORT).show();
                return;
            }

            // Continue only if the File was successfully created
            if (photoFile != null) {
                // Get the URI for the file using FileProvider
                currentPhotoUri = FileProvider.getUriForFile(requireContext(),
                        "com.example.expense_tracker.fileprovider",
                        photoFile);

                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, currentPhotoUri);
                cameraLauncher.launch(takePictureIntent);
            }
        } else {
            Toast.makeText(requireContext(), "No camera app found", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Create a unique image file to store the captured image
     */
    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = requireActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        // Create the file
        return File.createTempFile(
                imageFileName, /* prefix */
                ".jpg", /* suffix */
                storageDir /* directory */
        );
    }

    /**
     * Display the selected image in the preview
     */
    private void displaySelectedImage() {
        if (selectedImageUri != null) {
            cardReceiptPreview.setVisibility(View.VISIBLE);

            // Use Glide to load the image
            Glide.with(this)
                    .load(selectedImageUri)
                    .centerCrop()
                    .placeholder(R.drawable.ic_image_placeholder)
                    .error(R.drawable.ic_broken_image)
                    .into(ivReceiptPreview);
        }
    }

    @Override
    public void onDestroy() {

        super.onDestroy();
        // Shutdown ExecutorService when fragment is destroyed
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}