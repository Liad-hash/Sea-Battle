import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Ship {
    private final String name;
    private final int size;
    private final List<Coord> cells = new ArrayList<>();
    private final Set<String> hits = new HashSet<>();

    // creates a ship with a name and size (length).
    // the board will later assign its exact cells with placeAt().
    public Ship(String name, int size) {
        this.name = name;
        this.size = size;
    }

    // assigns the list of cells this ship occupies on the board.
    // call this only after validation that the placement is legal.
    public void placeAt(List<Coord> occupiedCells) {
        cells.clear();
        cells.addAll(occupiedCells);
    }

    // returns true if this ship occupies the given coordinate.
    // used by board logic to detect hits.
    public boolean occupies(Coord c) {
        for (Coord cell : cells) {
            if (cell.row == c.row && cell.col == c.col) return true;
        }
        return false;
    }

    // marks a hit on the given coordinate if it belongs to this ship.
    // returns true if a new hit was added, false otherwise.
    public boolean registerHit(Coord c) {
        if (!occupies(c)) return false;
        String key = c.row + "," + c.col;
        if (hits.contains(key)) return false;
        hits.add(key);
        return true;
    }

    // returns true if number of hits equals ship size.
    // used to announce that the ship is sunk.
    public boolean isSunk() {
        return hits.size() >= size;
    }

    // returns the ship name for messages / ui.
    public String getName() {
        return name;
    }

    // returns ship size (length).
    public int getSize() {
        return size;
    }

    // returns the occupied cells of the ship.
    // useful for debugging or revealing at end of game.
    public List<Coord> getCells() {
        return new ArrayList<>(cells);
    }
}
