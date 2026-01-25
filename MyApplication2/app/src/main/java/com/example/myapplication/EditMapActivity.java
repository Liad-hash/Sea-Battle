package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class EditMapActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_map);

        String usernameExtra = getIntent().getStringExtra("username");
        final String username = (usernameExtra == null || usernameExtra.trim().isEmpty())
                ? "player"
                : usernameExtra;

        ImageButton btnBack = findViewById(R.id.btnBack);
        Button btnApply = findViewById(R.id.btnApply);

        btnBack.setOnClickListener(v -> finish());

        btnApply.setOnClickListener(v -> {
            Toast.makeText(EditMapActivity.this, "map saved (ui only)", Toast.LENGTH_SHORT).show();
            Intent i = new Intent(EditMapActivity.this, LobbyActivity.class);
            i.putExtra("username", username);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(i);
        });
    }
}
