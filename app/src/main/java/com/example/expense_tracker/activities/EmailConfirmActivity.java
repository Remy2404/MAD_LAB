package com.example.expense_tracker.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.example.expense_tracker.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.SignInMethodQueryResult;

public class EmailConfirmActivity extends AppCompatActivity {

    private EditText etEmail;
    private Button btnContinue;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.confirm_email);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Initialize views
        etEmail = findViewById(R.id.editTextTextEmailAddress2);
        btnContinue = findViewById(R.id.button);

        btnContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = etEmail.getText().toString().trim();
                if (validateEmail(email)) {
                    checkEmailExists(email);
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

    private void checkEmailExists(String email) {
        mAuth.fetchSignInMethodsForEmail(email)
                .addOnCompleteListener(new OnCompleteListener<SignInMethodQueryResult>() {
                    @Override
                    public void onComplete(@NonNull Task<SignInMethodQueryResult> task) {
                        if (task.isSuccessful()) {
                            boolean isNewUser = task.getResult().getSignInMethods().isEmpty();
                            
                            if (isNewUser) {
                                // Email is not registered, proceed to sign up
                                Intent intent = new Intent(EmailConfirmActivity.this, SignUpActivity.class);
                                intent.putExtra("email", email);
                                startActivity(intent);
                            } else {
                                // Email already exists
                                etEmail.setError("Email already registered. Please login");
                            }
                        } else {
                            Toast.makeText(EmailConfirmActivity.this, "Error checking email: " + 
                                           task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}