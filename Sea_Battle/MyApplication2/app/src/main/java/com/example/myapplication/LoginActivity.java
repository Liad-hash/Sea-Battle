package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        ImageButton btnBack = findViewById(R.id.btnBack);
        Button btnDoLogin = findViewById(R.id.btnDoLogin);
        EditText etUsername = findViewById(R.id.etUsername);

        btnBack.setOnClickListener(v -> finish());

        btnDoLogin.setOnClickListener(v -> {
            // ui only: no firebase yet
            String username = etUsername.getText().toString().trim();
            if (username.isEmpty()) username = "player";

            Toast.makeText(this, "login (ui only)", Toast.LENGTH_SHORT).show();
            Intent i = new Intent(this, LobbyActivity.class);
            i.putExtra("username", username);
            startActivity(i);
            finish();
        });
    }
}
