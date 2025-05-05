package com.example.expense_tracker.activities;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.expense_tracker.R;

public class LoginSuccessActivity extends BaseActivity {

    private Button btnExplore;
    private TextView tvWelcomeMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_successfull);

        // Initialize views
        btnExplore = findViewById(R.id.button1);
        tvWelcomeMessage = findViewById(R.id.textView17);

        // Get user email from intent
        String userEmail = getIntent().getStringExtra("user_email");
        if (userEmail != null && !userEmail.isEmpty()) {
            String username = userEmail.substring(0, userEmail.indexOf('@'));
            tvWelcomeMessage.setText("Welcome " + username + "! You will be moved to home screen right now.");
        }

        btnExplore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateToMainActivity();
            }
        });

        // Auto navigate after delay
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!isFinishing()) {
                    navigateToMainActivity();
                }
            }
        }, 3000); // 3 seconds

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
    private void navigateToMainActivity() {
        Intent intent = new Intent(LoginSuccessActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}