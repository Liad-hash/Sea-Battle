package com.example.myapplication;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class BattleActivity extends AppCompatActivity {

    private boolean secretUsed = false;
    private SeaBattleBoardView enemyBoard;
    private SeaBattleBoardView yourBoard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // game screen should be landscape
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        setContentView(R.layout.activity_battle);

        String usernameExtra = getIntent().getStringExtra("username");
        final String username = (usernameExtra == null || usernameExtra.trim().isEmpty())
                ? "player"
                : usernameExtra;

        ImageButton btnExit = findViewById(R.id.btnExit);
        Button btnSecret = findViewById(R.id.btnSecret);
        Button btnFakeWin = findViewById(R.id.btnFakeWin);

        enemyBoard = findViewById(R.id.enemyBoard);
        yourBoard = findViewById(R.id.yourBoard);

        // ui demo: show your ships, hide enemy ships
        yourBoard.setShowShips(true);
        yourBoard.clearAll();
        yourBoard.loadDemoShips();

        enemyBoard.setShowShips(false);
        enemyBoard.clearAll();
        enemyBoard.loadDemoShips();

        enemyBoard.setOnCellTapListener((row, col) -> {
            SeaBattleBoardView.ShootResult res = enemyBoard.shootDetailed(row, col);
            if (!res.changed) return;

            if (res.hit && res.sunk) {
                Toast.makeText(this, "hit + sunk!", Toast.LENGTH_SHORT).show();
            } else if (res.hit) {
                Toast.makeText(this, "hit!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "miss", Toast.LENGTH_SHORT).show();
            }
        });

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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // don't keep the whole app locked in landscape if this activity goes away
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
    }
}
