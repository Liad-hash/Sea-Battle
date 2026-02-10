package com.example.myapplication;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * a drawable sea battle board (10x10)
 *
 * cell values (rendering):
 * 0 empty
 * 1 ship (only used for drawing when showShips=true)
 * 2 hit
 * 3 miss
 */
public class SeaBattleBoardView extends View {

    public static final int SIZE = 10;

    public static class ShootResult {
        /** true if the shot was applied (cell was not already hit/miss). */
        public final boolean applied;
        /** backwards compatible alias (older code used `changed`). */
        public final boolean changed;
        public final boolean hit;
        public final boolean sunk;

        public ShootResult(boolean applied, boolean hit, boolean sunk) {
            this.applied = applied;
            this.changed = applied;
            this.hit = hit;
            this.sunk = sunk;
        }
    }

    public interface OnCellTapListener {
        void onCellTapped(int row, int col);
    }

    private static class Ship {
        final int id;
        final ArrayList<int[]> cells = new ArrayList<>();
        int hitCount = 0;

        Ship(int id) {
            this.id = id;
        }

        boolean isSunk() {
            return hitCount >= cells.size() && !cells.isEmpty();
        }
    }

    private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF r = new RectF();

    // rendering state
    private final int[][] state = new int[SIZE][SIZE];

    // gameplay/editor model
    private final int[][] shipId = new int[SIZE][SIZE]; // 0 none, >0 ship id
    private final Map<Integer, Ship> ships = new HashMap<>();
    private int nextShipId = 1;

    private boolean showShips = true;
    private boolean editMode = false;
    private boolean enforceNoAdjacency = true;

    private @Nullable OnCellTapListener tapListener;

    // drawing geometry (computed on draw)
    private float originX, originY;
    private float cell;

    // ghost ship (editor)
    private boolean ghostActive = false;
    private int ghostLength = 0;
    private boolean ghostHorizontal = true;
    private int ghostRow = -1;
    private int ghostCol = -1;
    private boolean ghostValid = false;

    public SeaBattleBoardView(Context context) {
        super(context);
        init();
    }

    public SeaBattleBoardView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        setClickable(true);
        clear();
    }

    public void setOnCellTapListener(@Nullable OnCellTapListener l) {
        this.tapListener = l;
    }

    public void setShowShips(boolean showShips) {
        this.showShips = showShips;
        invalidate();
    }

    public void setEditMode(boolean editMode) {
        this.editMode = editMode;
        invalidate();
    }

    public void setEnforceNoAdjacency(boolean enforceNoAdjacency) {
        this.enforceNoAdjacency = enforceNoAdjacency;
    }

    /** backwards compatible alias. */
    public void clearAll() {
        clear();
    }

    public void clear() {
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                state[i][j] = 0;
                shipId[i][j] = 0;
            }
        }
        ships.clear();
        nextShipId = 1;
        clearGhost();
        invalidate();
    }

    /** ui demo ships */
    public void loadDemoShips() {
        clear();
        // classic sizes 4,3,2,1
        placeShip(1, 1, 4, false);
        placeShip(4, 5, 3, true);
        placeShip(7, 2, 2, true);
        placeShip(8, 8, 1, true);
    }

    /**
     * places a ship on the board.
     * rules: no overlap, (optionally) no adjacency (including diagonals)
     */
    public boolean placeShip(int row, int col, int length, boolean horizontal) {
        if (!canPlaceShip(row, col, length, horizontal, enforceNoAdjacency)) return false;

        int id = nextShipId++;
        Ship s = new Ship(id);

        for (int k = 0; k < length; k++) {
            int rr = row + (horizontal ? 0 : k);
            int cc = col + (horizontal ? k : 0);
            shipId[rr][cc] = id;
            state[rr][cc] = 1;
            s.cells.add(new int[]{rr, cc});
        }

        ships.put(id, s);
        invalidate();
        return true;
    }

    public boolean canPlaceShip(int row, int col, int length, boolean horizontal, boolean noAdjacency) {
        if (length < 1 || length > 4) return false;
        if (!inBounds(row, col)) return false;

        int endRow = row + (horizontal ? 0 : (length - 1));
        int endCol = col + (horizontal ? (length - 1) : 0);
        if (endRow >= SIZE || endCol >= SIZE) return false;

        // overlap
        for (int k = 0; k < length; k++) {
            int rr = row + (horizontal ? 0 : k);
            int cc = col + (horizontal ? k : 0);
            if (shipId[rr][cc] != 0) return false;
            if (state[rr][cc] == 2) return false;
        }

        if (!noAdjacency) return true;

        // adjacency check (including diagonals)
        for (int k = 0; k < length; k++) {
            int rr = row + (horizontal ? 0 : k);
            int cc = col + (horizontal ? k : 0);

            for (int dr = -1; dr <= 1; dr++) {
                for (int dc = -1; dc <= 1; dc++) {
                    int nr = rr + dr;
                    int nc = cc + dc;
                    if (!inBounds(nr, nc)) continue;
                    if (shipId[nr][nc] != 0) return false;
                }
            }
        }

        return true;
    }

    public void markMiss(int row, int col) {
        if (!inBounds(row, col)) return;
        if (state[row][col] == 2) return;
        if (state[row][col] == 3) return;
        state[row][col] = 3;
        invalidate();
    }

    /**
     * shoot on a cell.
     * - if a ship was sunk, it will automatically mark all surrounding cells as miss (classic rule: no adjacent ships)
     */
    public ShootResult shootDetailed(int row, int col) {
        if (!inBounds(row, col)) return new ShootResult(false, false, false);
        if (state[row][col] == 2 || state[row][col] == 3) return new ShootResult(false, false, false);

        int sid = shipId[row][col];
        if (sid != 0) {
            state[row][col] = 2;
            Ship s = ships.get(sid);
            if (s != null) {
                s.hitCount++;
                if (s.isSunk()) {
                    markSurroundingMiss(s);
                    invalidate();
                    return new ShootResult(true, true, true);
                }
            }
            invalidate();
            return new ShootResult(true, true, false);
        } else {
            state[row][col] = 3;
            invalidate();
            return new ShootResult(true, false, false);
        }
    }

    /** legacy helper: returns only hit/miss */
    public boolean shoot(int row, int col) {
        return shootDetailed(row, col).hit;
    }

    private void markSurroundingMiss(Ship s) {
        for (int[] cell : s.cells) {
            int rr = cell[0];
            int cc = cell[1];
            for (int dr = -1; dr <= 1; dr++) {
                for (int dc = -1; dc <= 1; dc++) {
                    int nr = rr + dr;
                    int nc = cc + dc;
                    if (!inBounds(nr, nc)) continue;
                    if (state[nr][nc] == 2) continue; // don't overwrite hits
                    if (shipId[nr][nc] != 0) continue; // don't mark ship cells as miss
                    state[nr][nc] = 3;
                }
            }
        }
    }

    // editor ghost api

    public void setGhostShip(int length, boolean horizontal) {
        ghostActive = true;
        ghostLength = length;
        ghostHorizontal = horizontal;
        invalidate();
    }

    public void clearGhost() {
        ghostActive = false;
        ghostLength = 0;
        ghostRow = -1;
        ghostCol = -1;
        ghostValid = false;
        invalidate();
    }

    public void updateGhostHover(int row, int col) {
        ghostRow = row;
        ghostCol = col;
        ghostValid = ghostActive && canPlaceShip(row, col, ghostLength, ghostHorizontal, true);
        invalidate();
    }

    public boolean tryPlaceGhost() {
        if (!ghostActive) return false;
        if (!inBounds(ghostRow, ghostCol)) return false;
        if (!ghostValid) return false;
        boolean ok = placeShip(ghostRow, ghostCol, ghostLength, ghostHorizontal);
        if (ok) {
            updateGhostHover(ghostRow, ghostCol);
        }
        return ok;
    }

    private boolean inBounds(int r, int c) {
        return r >= 0 && c >= 0 && r < SIZE && c < SIZE;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float w = getWidth();
        float h = getHeight();

        float side = Math.min(w, h);
        cell = side / SIZE;
        originX = (w - side) / 2f;
        originY = (h - side) / 2f;

        // background
        p.setStyle(Paint.Style.FILL);
        p.setColor(0xFFF7F3E8);
        canvas.drawRect(originX, originY, originX + side, originY + side, p);

        // ships (under grid)
        if (showShips) {
            p.setColor(0xFF2E6BD6);
            for (int row = 0; row < SIZE; row++) {
                for (int col = 0; col < SIZE; col++) {
                    if (shipId[row][col] != 0 && state[row][col] != 2) {
                        float pad = cell * 0.12f;
                        r.set(
                                originX + col * cell + pad,
                                originY + row * cell + pad,
                                originX + (col + 1) * cell - pad,
                                originY + (row + 1) * cell - pad
                        );
                        canvas.drawRoundRect(r, cell * 0.25f, cell * 0.25f, p);
                    }
                }
            }
        }

        // ghost ship overlay (editor)
        if (editMode && ghostActive && inBounds(ghostRow, ghostCol)) {
            int alpha = 110;
            int color = ghostValid ? 0xFF2DA44E : 0xFFD43B3B;
            p.setStyle(Paint.Style.FILL);
            p.setColor((alpha << 24) | (color & 0x00FFFFFF));

            for (int k = 0; k < ghostLength; k++) {
                int rr = ghostRow + (ghostHorizontal ? 0 : k);
                int cc = ghostCol + (ghostHorizontal ? k : 0);
                if (!inBounds(rr, cc)) continue;
                float pad = cell * 0.08f;
                r.set(
                        originX + cc * cell + pad,
                        originY + rr * cell + pad,
                        originX + (cc + 1) * cell - pad,
                        originY + (rr + 1) * cell - pad
                );
                canvas.drawRoundRect(r, cell * 0.20f, cell * 0.20f, p);
            }
        }

        // hits / misses
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                int v = state[row][col];
                if (v == 2) {
                    drawX(canvas, row, col);
                } else if (v == 3) {
                    drawDot(canvas, row, col);
                }
            }
        }

        // grid lines
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(Math.max(2f, cell * 0.04f));
        p.setColor(0xFF3B2F2F);

        for (int i = 0; i <= SIZE; i++) {
            float x = originX + i * cell;
            float y = originY + i * cell;
            canvas.drawLine(originX, y, originX + side, y, p);
            canvas.drawLine(x, originY, x, originY + side, p);
        }
    }

    private void drawX(Canvas canvas, int row, int col) {
        float left = originX + col * cell;
        float top = originY + row * cell;
        float pad = cell * 0.18f;

        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(Math.max(3f, cell * 0.08f));
        p.setColor(0xFFD43B3B);
        canvas.drawLine(left + pad, top + pad, left + cell - pad, top + cell - pad, p);
        canvas.drawLine(left + cell - pad, top + pad, left + pad, top + cell - pad, p);
    }

    private void drawDot(Canvas canvas, int row, int col) {
        float cx = originX + (col + 0.5f) * cell;
        float cy = originY + (row + 0.5f) * cell;

        p.setStyle(Paint.Style.FILL);
        p.setColor(0xFF3B2F2F);
        canvas.drawCircle(cx, cy, cell * 0.10f, p);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (editMode) {
            // editor placement is handled via drag&drop from EditMapActivity
            return true;
        }

        if (event.getAction() != MotionEvent.ACTION_UP) return true;
        if (cell <= 0f) return true;

        float x = event.getX();
        float y = event.getY();

        int col = (int) ((x - originX) / cell);
        int row = (int) ((y - originY) / cell);

        if (!inBounds(row, col)) return true;
        if (tapListener != null) {
            tapListener.onCellTapped(row, col);
        }
        return true;
    }

    // helpers for drag
    public int pointToRow(float y) {
        if (cell <= 0f) return -1;
        return (int) ((y - originY) / cell);
    }

    public int pointToCol(float x) {
        if (cell <= 0f) return -1;
        return (int) ((x - originX) / cell);
    }
}
