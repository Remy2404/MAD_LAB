package com.example.expense_tracker.activities;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import com.example.expense_tracker.R;

public class IntroActivity extends AppCompatActivity {

    private static final String PREF_NAME = "expense_tracker_prefs";
    private static final String IS_LOGGED_IN = "is_logged_in";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);

        // Initialize preferences as local variable
        SharedPreferences preferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);

        // Check if user is already logged in
        if (preferences.getBoolean(IS_LOGGED_IN, false)) {
            // Delay for splash screen effect with non-deprecated Handler
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                startActivity(new Intent(IntroActivity.this, MainActivity.class));
                finish();
            }, 1500);
            return;
        }

        // Find the button by ID as local variable
        findViewById(R.id.button2).setOnClickListener(v -> {
            // Navigate to login screen
            Intent intent = new Intent(IntroActivity.this, LoginActivity.class);
            startActivity(intent);
        });
    }
}