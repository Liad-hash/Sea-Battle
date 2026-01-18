public enum ShotResult {
    INVALID,      // out of bounds
    ALREADY_TRIED,// already shot here before
    MISS,         // water
    HIT,          // hit but not sunk
    SUNK          // ship is fully destroyed
}
