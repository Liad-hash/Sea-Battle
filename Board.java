import java.util.*;

public class Board {
    public static final char WATER = '~';
    public static final char SHIP  = 'S'; // only for internal board
    public static final char HIT   = 'X';
    public static final char MISS  = 'o';

    private final int size;
    private final char[][] grid;
    private final List<Ship> ships = new ArrayList<>();
    private final boolean[][] shots; // tracks tried positions

    // creates a square board of size n filled with water.
    // also initializes a shots map to prevent repeated shooting.
    public Board(int size) {
        this.size = size;
        this.grid = new char[size][size];
        this.shots = new boolean[size][size];
        clear();
    }

    // resets the board to water and clears all ships and shots.
    // useful for restarting a new game quickly.
    public void clear() {
        ships.clear();
        for (int r = 0; r < size; r++) {
            Arrays.fill(grid[r], WATER);
            Arrays.fill(shots[r], false);
        }
    }

    // returns the board size (n for an n x n board).
    // used by game to validate inputs.
    public int getSize() {
        return size;
    }

    // checks if a ship can be placed starting at (row,col) with given orientation.
    // this verifies bounds and that no existing ship overlaps.
    public boolean canPlaceShip(int row, int col, int shipSize, Orientation ori) {
        List<Coord> cells = computeCells(row, col, shipSize, ori);
        if (cells == null) return false;
        for (Coord c : cells) {
            if (grid[c.row][c.col] != WATER) return false;
        }
        return true;
    }

    // places a ship on the board after checking it is legal.
    // returns true if placed, false if placement is illegal.
    public boolean placeShip(Ship ship, int row, int col, Orientation ori) {
        if (!canPlaceShip(row, col, ship.getSize(), ori)) return false;

        List<Coord> cells = computeCells(row, col, ship.getSize(), ori);
        ship.placeAt(cells);
        ships.add(ship);

        for (Coord c : cells) {
            grid[c.row][c.col] = SHIP;
        }
        return true;
    }

    // randomly places a ship by trying random positions until it fits.
    // this is a simple helper for quick setup.
    public void placeShipRandomly(Ship ship, Random rnd) {
        while (true) {
            Orientation ori = rnd.nextBoolean() ? Orientation.HORIZONTAL : Orientation.VERTICAL;
            int row = rnd.nextInt(size);
            int col = rnd.nextInt(size);
            if (placeShip(ship, row, col, ori)) return;
        }
    }

    // fires at a coordinate and returns the result (miss/hit/sunk/etc).
    // this does not allow shooting the same cell twice.
    public ShotResult shoot(int row, int col) {
        if (row < 0 || row >= size || col < 0 || col >= size) return ShotResult.INVALID;
        if (shots[row][col]) return ShotResult.ALREADY_TRIED;

        shots[row][col] = true;

        if (grid[row][col] == SHIP) {
            grid[row][col] = HIT;

            Ship hitShip = findShipAt(new Coord(row, col));
            if (hitShip != null) {
                hitShip.registerHit(new Coord(row, col));
                if (hitShip.isSunk()) return ShotResult.SUNK;
            }
            return ShotResult.HIT;
        } else if (grid[row][col] == WATER) {
            grid[row][col] = MISS;
            return ShotResult.MISS;
        } else {
            // if grid already has HIT/MISS but shots was false (should not happen), treat as already tried.
            return ShotResult.ALREADY_TRIED;
        }
    }

    // returns true if all ships on this board are sunk.
    // used to detect game over.
    public boolean allShipsSunk() {
        for (Ship s : ships) {
            if (!s.isSunk()) return false;
        }
        return !ships.isEmpty();
    }

    // creates a view of the board for an opponent (ships are hidden).
    // shows only hits (x) and misses (o) and water (~).
    public char[][] getFogOfWarView() {
        char[][] view = new char[size][size];
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                char cell = grid[r][c];
                if (cell == HIT) view[r][c] = HIT;
                else if (cell == MISS) view[r][c] = MISS;
                else view[r][c] = WATER;
            }
        }
        return view;
    }

    // returns the full board view including ships (for your own board).
    // useful for debugging or rendering your own board.
    public char[][] getFullView() {
        char[][] view = new char[size][size];
        for (int r = 0; r < size; r++) {
            System.arraycopy(grid[r], 0, view[r], 0, size);
        }
        return view;
    }

    // pretty prints a given board view to the console.
    // you can pass fog-of-war for enemy, or full view for yourself.
    public static void printView(char[][] view) {
        int n = view.length;
        System.out.print("   ");
        for (int c = 0; c < n; c++) System.out.print(c + " ");
        System.out.println();
        for (int r = 0; r < n; r++) {
            System.out.printf("%2d ", r);
            for (int c = 0; c < n; c++) {
                System.out.print(view[r][c] + " ");
            }
            System.out.println();
        }
    }

    // computes the list of cells for a ship placement or returns null if out of bounds.
    // centralizes the placement math for both canPlaceShip and placeShip.
    private List<Coord> computeCells(int row, int col, int shipSize, Orientation ori) {
    List<Coord> cells = new ArrayList<>();

    for (int i = 0; i < shipSize; i++) {
        int rr = row;
        int cc = col;

        if (ori == Orientation.VERTICAL) {
            rr = row + i;
        } 
        else if (ori == Orientation.HORIZONTAL) {
            cc = col + i;
        }

        Coord c = new Coord(rr, cc);
        if (!c.inBounds(size)) return null;
        cells.add(c);
    }

    return cells;
}


    // finds which ship is located at a coordinate (if any).
    // used after a hit to mark sunk/hit state.
    private Ship findShipAt(Coord c) {
        for (Ship s : ships) {
            if (s.occupies(c)) return s;
        }
        return null;
    }
}
