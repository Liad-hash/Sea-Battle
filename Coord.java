public class Coord {
    public final int row;
    public final int col;

    // creates an immutable coordinate (row, col) on the board.
    // use this to pass positions between functions safely.
    public Coord(int row, int col) {
        this.row = row;
        this.col = col;
    }

    // checks if this coordinate is inside a board of size n.
    // returns true if 0 <= row/col < n.
    public boolean inBounds(int n) {
        return row >= 0 && row < n && col >= 0 && col < n;
    }

    @Override
    public String toString() {
        return "(" + row + "," + col + ")";
    }
}
