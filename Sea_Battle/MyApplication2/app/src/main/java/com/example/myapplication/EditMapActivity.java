package com.example.myapplication;

import android.content.ClipData;
import android.content.Intent;
import android.os.Bundle;
import android.view.DragEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class EditMapActivity extends AppCompatActivity {

    private SeaBattleBoardView board;

    private boolean horizontal = true;

    // classic counts: 4x1, 3x2, 2x3, 1x4
    private int count4 = 1;
    private int count3 = 2;
    private int count2 = 3;
    private int count1 = 4;

    private Button btnRotate;
    private Button btnShip4;
    private Button btnShip3;
    private Button btnShip2;
    private Button btnShip1;

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

        btnRotate = findViewById(R.id.btnRotate);
        btnShip4 = findViewById(R.id.btnShip4);
        btnShip3 = findViewById(R.id.btnShip3);
        btnShip2 = findViewById(R.id.btnShip2);
        btnShip1 = findViewById(R.id.btnShip1);

        board = findViewById(R.id.boardView);
        board.setEditMode(true);
        board.setShowShips(true);
        board.setEnforceNoAdjacency(true);
        board.clear();

        refreshShipButtons();

        btnBack.setOnClickListener(v -> finish());

        btnRotate.setOnClickListener(v -> {
            horizontal = !horizontal;
            btnRotate.setText(horizontal ? "rotate: horizontal" : "rotate: vertical");
        });

        // start drag (long press) from palette
        setupShipDrag(btnShip4, 4);
        setupShipDrag(btnShip3, 3);
        setupShipDrag(btnShip2, 2);
        setupShipDrag(btnShip1, 1);

        // handle drop on board
        board.setOnDragListener((v, event) -> handleBoardDrag(event));

        btnApply.setOnClickListener(v -> {
            // optional: validate that all ships were placed
            if (count4 != 0 || count3 != 0 || count2 != 0 || count1 != 0) {
                Toast.makeText(EditMapActivity.this, "place all ships first", Toast.LENGTH_SHORT).show();
                return;
            }

            Toast.makeText(EditMapActivity.this, "map saved (ui only)", Toast.LENGTH_SHORT).show();
            Intent i = new Intent(EditMapActivity.this, LobbyActivity.class);
            i.putExtra("username", username);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(i);
        });
    }

    private void setupShipDrag(View shipBtn, int length) {
        shipBtn.setOnLongClickListener(v -> {
            if (!hasRemaining(length)) {
                Toast.makeText(EditMapActivity.this, "no more ships of size " + length, Toast.LENGTH_SHORT).show();
                return true;
            }

            // prepare ghost so you see preview immediately when you enter the board
            board.setGhostShip(length, horizontal);

            ClipData data = ClipData.newPlainText("ship_length", String.valueOf(length));
            View.DragShadowBuilder shadow = new View.DragShadowBuilder(v);
            v.startDragAndDrop(data, shadow, null, 0);
            return true;
        });
    }

    private boolean handleBoardDrag(DragEvent event) {
        ClipData cd = event.getClipData();
        int length = -1;
        if (cd != null && cd.getItemCount() > 0) {
            try {
                length = Integer.parseInt(String.valueOf(cd.getItemAt(0).getText()));
            } catch (Exception ignored) {
            }
        }

        switch (event.getAction()) {
            case DragEvent.ACTION_DRAG_STARTED:
                // accept only our drags
                return length >= 1 && length <= 4;

            case DragEvent.ACTION_DRAG_ENTERED:
                if (length != -1) board.setGhostShip(length, horizontal);
                return true;

            case DragEvent.ACTION_DRAG_LOCATION: {
                if (length == -1) return true;
                int row = board.pointToRow(event.getY());
                int col = board.pointToCol(event.getX());
                if (row >= 0 && col >= 0) {
                    board.setGhostShip(length, horizontal);
                    board.updateGhostHover(row, col);
                }
                return true;
            }

            case DragEvent.ACTION_DROP: {
                if (length == -1) return true;
                int row = board.pointToRow(event.getY());
                int col = board.pointToCol(event.getX());
                if (row >= 0 && col >= 0) {
                    board.setGhostShip(length, horizontal);
                    board.updateGhostHover(row, col);
                    boolean ok = board.tryPlaceGhost();
                    if (ok) {
                        consume(length);
                        refreshShipButtons();
                    } else {
                        Toast.makeText(this, "can't place there", Toast.LENGTH_SHORT).show();
                    }
                }
                board.clearGhost();
                return true;
            }

            case DragEvent.ACTION_DRAG_ENDED:
                board.clearGhost();
                return true;
        }
        return true;
    }

    private boolean hasRemaining(int length) {
        switch (length) {
            case 4: return count4 > 0;
            case 3: return count3 > 0;
            case 2: return count2 > 0;
            case 1: return count1 > 0;
            default: return false;
        }
    }

    private void consume(int length) {
        switch (length) {
            case 4: if (count4 > 0) count4--; break;
            case 3: if (count3 > 0) count3--; break;
            case 2: if (count2 > 0) count2--; break;
            case 1: if (count1 > 0) count1--; break;
        }
    }

    private void refreshShipButtons() {
        btnShip4.setText("size 4 (x" + count4 + ")\n(long press)");
        btnShip3.setText("size 3 (x" + count3 + ")\n(long press)");
        btnShip2.setText("size 2 (x" + count2 + ")\n(long press)");
        btnShip1.setText("size 1 (x" + count1 + ")\n(long press)");

        btnShip4.setEnabled(count4 > 0);
        btnShip3.setEnabled(count3 > 0);
        btnShip2.setEnabled(count2 > 0);
        btnShip1.setEnabled(count1 > 0);
    }
}
