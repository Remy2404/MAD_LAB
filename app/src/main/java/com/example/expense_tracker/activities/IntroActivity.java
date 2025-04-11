package com.example.expense_tracker.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

import com.example.expense_tracker.R;

public class IntroActivity extends AppCompatActivity {

    private static final String PREF_NAME = "expense_tracker_prefs";
    private static final String IS_LOGGED_IN = "is_logged_in";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);

        View rootView = findViewById(android.R.id.content);
        if (rootView == null) {
            finish();
            return;
        }

        SharedPreferences preferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);

        if (preferences.getBoolean(IS_LOGGED_IN, false)) {
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                startActivity(new Intent(IntroActivity.this, MainActivity.class));
                finish();
            }, 1500);
            return;
        }

        View button = findViewById(R.id.button2);
        if (button != null) {
            button.setOnClickListener(v -> {
                startActivity(new Intent(IntroActivity.this, LoginActivity.class));
            });
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