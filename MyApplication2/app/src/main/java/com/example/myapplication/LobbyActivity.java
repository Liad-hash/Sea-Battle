package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class LobbyActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lobby);

        String usernameExtra = getIntent().getStringExtra("username");
        final String username = (usernameExtra == null || usernameExtra.trim().isEmpty())
                ? "player"
                : usernameExtra;

        TextView tvHello = findViewById(R.id.tvHello);
        tvHello.setText("welcome, " + username);

        Button btnEditMap = findViewById(R.id.btnEditMap);
        Button btnBattle = findViewById(R.id.btnBattle);

        btnEditMap.setOnClickListener(v -> {
            Intent i = new Intent(LobbyActivity.this, EditMapActivity.class);
            i.putExtra("username", username);
            startActivity(i);
        });

        btnBattle.setOnClickListener(v -> {
            Intent i = new Intent(LobbyActivity.this, BattleActivity.class);
            i.putExtra("username", username);
            startActivity(i);
        });
    }
}
