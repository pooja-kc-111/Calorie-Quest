package com.example.caloriequest;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class WelcomeActivity extends AppCompatActivity {

    private EditText nameInput;
    private Button getStartedButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        nameInput = findViewById(R.id.nameInput);
        getStartedButton = findViewById(R.id.getStartedButton);

        SharedPreferences prefs = getSharedPreferences(
                "CalorieQuestData", MODE_PRIVATE);

        String savedName = prefs.getString("userName", "");
        nameInput.setText(savedName);

        getStartedButton.setOnClickListener(view -> {
            String name = nameInput.getText().toString().trim();

            if (name.isEmpty()) {
                nameInput.setError("Please enter your name 💗");
                nameInput.requestFocus();
                return;
            }

            prefs.edit().putString("userName", name).apply();

            Intent intent = new Intent(
                    WelcomeActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });
    }
}
