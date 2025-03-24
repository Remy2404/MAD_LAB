package com.example.expense_tracker.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.expense_tracker.R;
import java.io.FileOutputStream;

public class SignUpActivity extends AppCompatActivity {

    private EditText etFirstName, etLastName, etPassword, etConfirmPassword;
    private Button btnContinue;
    private String email;
    private static final String DATA_FILE = "data.txt";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // Get email from intent
        email = getIntent().getStringExtra("email");
        if (email == null || email.isEmpty()) {
            Toast.makeText(this, "Email information missing", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize views
        etFirstName = findViewById(R.id.textInputEditText);
        etLastName = findViewById(R.id.textInputEditText1);
        etPassword = findViewById(R.id.textInputEditText2);
        etConfirmPassword = findViewById(R.id.textInputEditText3);
        btnContinue = findViewById(R.id.button_continue);
        btnContinue.setOnClickListener(v -> {
            String firstName = etFirstName.getText().toString().trim();
            String lastName = etLastName.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String confirmPassword = etConfirmPassword.getText().toString().trim();

            if (validateInput(firstName, lastName, password, confirmPassword)) {
                if (saveUserToFile(email, password, firstName, lastName)) {
                    // Navigate to login success screen
                    Intent intent = new Intent(SignUpActivity.this, LoginSuccessActivity.class);
                    intent.putExtra("user_email", email);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(SignUpActivity.this,
                            "Failed to create account. Please try again.",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private boolean validateInput(String firstName, String lastName, String password, String confirmPassword) {
        if (firstName.isEmpty()) {
            etFirstName.setError("First name is required");
            return false;
        }

        if (lastName.isEmpty()) {
            etLastName.setError("Last name is required");
            return false;
        }

        if (password.isEmpty()) {
            etPassword.setError("Password is required");
            return false;
        }

        if (password.length() < 6) {
            etPassword.setError("Password must be at least 6 characters");
            return false;
        }

        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError("Passwords do not match");
            return false;
        }

        return true;
    }

    private boolean saveUserToFile(String email, String password, String firstName, String lastName) {
        try {
            // Format: email,password,firstName,lastName
            String userData = email + "," + password + "," + firstName + "," + lastName + "\n";

            FileOutputStream fos = openFileOutput(DATA_FILE, MODE_APPEND);
            fos.write(userData.getBytes());
            fos.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}