package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class BattleActivity extends AppCompatActivity {

    private boolean secretUsed = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_battle);

        String usernameExtra = getIntent().getStringExtra("username");
        final String username = (usernameExtra == null || usernameExtra.trim().isEmpty())
                ? "player"
                : usernameExtra;

        ImageButton btnExit = findViewById(R.id.btnExit);
        Button btnSecret = findViewById(R.id.btnSecret);
        Button btnFakeWin = findViewById(R.id.btnFakeWin);

        btnExit.setOnClickListener(v -> {
            Intent i = new Intent(this, LobbyActivity.class);
            i.putExtra("username", username);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(i);
            finish();
        });

        btnSecret.setOnClickListener(v -> {
            if (secretUsed) {
                Toast.makeText(this, "secret weapon already used", Toast.LENGTH_SHORT).show();
                return;
            }
            secretUsed = true;
            Toast.makeText(this, "secret weapon (ui only)", Toast.LENGTH_SHORT).show();
        });

        // debug: go to win screen without game logic
        btnFakeWin.setOnClickListener(v -> {
            Intent i = new Intent(this, WinActivity.class);
            i.putExtra("winner", username);
            startActivity(i);
            finish();
        });
    }
}
