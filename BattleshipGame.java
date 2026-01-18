import java.util.*;

public class BattleshipGame {
    private final Board playerBoard;
    private final Board cpuBoard;
    private final Random rnd = new Random();
    private final Scanner sc = new Scanner(System.in);

    // creates a new game with two boards: player and cpu.
    // this class runs turns and decides when the game ends.
    public BattleshipGame(int boardSize) {
        playerBoard = new Board(boardSize);
        cpuBoard = new Board(boardSize);
    }

    // sets up ships for both player and cpu in a simple way.
    // player places manually, cpu places randomly.
    public void setup() {
        List<Ship> fleet = defaultFleet();

        System.out.println("place your ships (row col orientation[h/v])");
        for (Ship s : fleet) {
            placeShipManual(playerBoard, new Ship(s.getName(), s.getSize()));
            Board.printView(playerBoard.getFullView());
            System.out.println();
        }

        for (Ship s : fleet) {
            cpuBoard.placeShipRandomly(new Ship(s.getName(), s.getSize()), rnd);
        }
    }

    // runs the main loop: player shoots, then cpu shoots, until someone wins.
    // prints simple boards to help you see progress.
    public void play() {
        while (true) {
            System.out.println("\n--- enemy board (fog) ---");
            Board.printView(cpuBoard.getFogOfWarView());

            if (playerTurn()) {
                System.out.println("you win!");
                break;
            }

            if (cpuTurn()) {
                System.out.println("cpu wins!");
                break;
            }

            System.out.println("\n--- your board ---");
            Board.printView(playerBoard.getFullView());
        }
    }

    // handles player input and fires at cpu board.
    // returns true if player just won.
    private boolean playerTurn() {
        while (true) {
            System.out.print("\nyour shot (row col): ");
            int row = sc.nextInt();
            int col = sc.nextInt();

            ShotResult res = cpuBoard.shoot(row, col);
            if (res == ShotResult.INVALID) {
                System.out.println("invalid (out of bounds). try again.");
                continue;
            }
            if (res == ShotResult.ALREADY_TRIED) {
                System.out.println("already tried. choose another cell.");
                continue;
            }

            System.out.println("result: " + res);
            return cpuBoard.allShipsSunk();
        }
    }

    // cpu picks a random cell it has not tried on the player board and shoots it.
    // returns true if cpu just won.
    private boolean cpuTurn() {
        while (true) {
            int row = rnd.nextInt(playerBoard.getSize());
            int col = rnd.nextInt(playerBoard.getSize());

            ShotResult res = playerBoard.shoot(row, col);
            if (res == ShotResult.ALREADY_TRIED || res == ShotResult.INVALID) continue;

            System.out.println("\ncpu shot " + row + " " + col + " -> " + res);
            return playerBoard.allShipsSunk();
        }
    }

    // asks the user for a ship placement and keeps retrying until it works.
    // accepts orientation as 'h' or 'v' for simplicity.
    private void placeShipManual(Board board, Ship ship) {
        while (true) {
            System.out.print(ship.getName() + " size=" + ship.getSize() + " placement: ");
            int row = sc.nextInt();
            int col = sc.nextInt();
            String o = sc.next().toLowerCase();

            Orientation ori = o.startsWith("v") ? Orientation.VERTICAL : Orientation.HORIZONTAL;

            boolean ok = board.placeShip(ship, row, col, ori);
            if (ok) return;

            System.out.println("cannot place there (overlap or out of bounds). try again.");
        }
    }

    // builds the default fleet list (names + sizes) used by both sides.
    // you can change sizes here without touching other logic.
    private List<Ship> defaultFleet() {
        return Arrays.asList(
                new Ship("carrier", 5),
                new Ship("battleship", 4),
                new Ship("cruiser", 3),
                new Ship("submarine", 3),
                new Ship("destroyer", 2)
        );
    }

    // entry point to run the game quickly from console.
    // creates game, sets up boards, and starts play loop.
    public static void main(String[] args) {
        BattleshipGame game = new BattleshipGame(10);
        game.setup();
        game.play();
    }
}
