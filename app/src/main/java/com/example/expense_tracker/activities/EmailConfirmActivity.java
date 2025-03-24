package com.example.expense_tracker.activities;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.expense_tracker.R;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;

public class EmailConfirmActivity extends AppCompatActivity {

    private EditText etEmail;
    private Button btnContinue;
    private static final String DATA_FILE = "data.txt";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.confirm_email);

        // Initialize views
        etEmail = findViewById(R.id.editTextTextEmailAddress2);
        btnContinue = findViewById(R.id.button);

        btnContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = etEmail.getText().toString().trim();

                if (validateEmail(email)) {
                    if (isEmailAlreadyRegistered(email)) {
                        Toast.makeText(EmailConfirmActivity.this,
                                "This email is already registered", Toast.LENGTH_SHORT).show();
                    } else {
                        // Navigate to sign up screen with email
                        Intent intent = new Intent(EmailConfirmActivity.this, SignUpActivity.class);
                        intent.putExtra("email", email);
                        startActivity(intent);
                    }
                }
            }
        });
    }

    private boolean validateEmail(String email) {
        if (email.isEmpty()) {
            etEmail.setError("Email is required");
            return false;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Please enter a valid email");
            return false;
        }

        return true;
    }

    private boolean isEmailAlreadyRegistered(String email) {
        try {
            FileInputStream fis = openFileInput(DATA_FILE);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader br = new BufferedReader(isr);

            String line;
            while ((line = br.readLine()) != null) {
                String[] userData = line.split(",");
                // Format: email,password,firstName,lastName
                if (userData.length >= 1 && userData[0].equals(email)) {
                    br.close();
                    return true;
                }
            }

            br.close();
        } catch (Exception e) {
            // File might not exist yet, which is fine
        }

        return false;
    }
}