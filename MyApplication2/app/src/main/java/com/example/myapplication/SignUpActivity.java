package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class SignUpActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        ImageButton btnBack = findViewById(R.id.btnBack);
        Button btnDoSignup = findViewById(R.id.btnDoSignup);
        EditText etUsername = findViewById(R.id.etUsername);

        btnBack.setOnClickListener(v -> finish());

        btnDoSignup.setOnClickListener(v -> {
            // ui only: no firebase yet
            String username = etUsername.getText().toString().trim();
            if (username.isEmpty()) username = "player";

            Toast.makeText(this, "signup (ui only)", Toast.LENGTH_SHORT).show();
            Intent i = new Intent(this, LobbyActivity.class);
            i.putExtra("username", username);
            startActivity(i);
            finish();
        });
    }
}
