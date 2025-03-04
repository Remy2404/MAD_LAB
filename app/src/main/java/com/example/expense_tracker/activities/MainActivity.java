package com.example.expense_tracker;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private EditText amountInput, notesInput;
    private Spinner categorySpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Reference the views
        notesInput = findViewById(R.id.notesInput);
        categorySpinner = findViewById(R.id.spinner2);
        Button addExpenseButton = findViewById(R.id.addExpenseButton);

        // Set up the Spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.categories, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(adapter);

        // Handle button click
        addExpenseButton.setOnClickListener(v -> addExpense());
    }

    private void addExpense() {
        String amount = amountInput.getText().toString();
        String category = categorySpinner.getSelectedItem().toString();
        String notes = notesInput.getText().toString();

        // Basic input validation
        if (amount.isEmpty() || category.isEmpty()) {
            Toast.makeText(this, "Please fill in all required fields.", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            double amountValue = Double.parseDouble(amount);

            @SuppressLint("DefaultLocale") String message = String.format("Expense Added:\nAmount: %.2f\nCategory: %s\nNotes: %s",
                    amountValue, category, notes);

            Toast.makeText(this, message, Toast.LENGTH_LONG).show();

            // Clear fields after adding
            amountInput.setText("");
            categorySpinner.setSelection(0);
            notesInput.setText("");

        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid amount format.", Toast.LENGTH_SHORT).show();
        }
    }
}