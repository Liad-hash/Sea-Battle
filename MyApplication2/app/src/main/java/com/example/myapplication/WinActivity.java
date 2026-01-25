package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class WinActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_win);

        String winner = getIntent().getStringExtra("winner");
        if (winner == null || winner.trim().isEmpty()) winner = "player";

        TextView tvWin = findViewById(R.id.tvWin);
        tvWin.setText(winner + " win");

        Button btnReturn = findViewById(R.id.btnReturnLobby);
        btnReturn.setOnClickListener(v -> {
            Intent i = new Intent(this, LobbyActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(i);
            finish();
        });
    }
}
