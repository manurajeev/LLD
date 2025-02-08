/*
  ┌─────────────┐        ┌─────────────┐ 
 │     Game    │        │   Player    │
 ├─────────────┤        ├─────────────┤
 │ - board: Board       │ - symbol: char
 │ - players: List<Player>           │
 │ - currentPlayer: int │ + getSymbol(): char
 │ + startGame()        │
 │ + playTurn()         │
 │ + isOver(): boolean  │
 └─────────────┘        └─────────────┘
         │
         │ uses
         ▼
 ┌─────────────┐
 │    Board    │
 ├─────────────┤
 │ - grid: Cell[][] 
 │ + initializeBoard() 
 │ + isValidMove(row, col): boolean  
 │ + placeMark(move: Move): void     
 │ + checkWin(move: Move): boolean   
 │ + isBoardFull(): boolean          
 └─────────────┘
        ▲
        │ contains
        │
 ┌─────────────┐
 │    Cell     │
 ├─────────────┤
 │ - row: int  
 │ - col: int
 │ - symbol: char (e.g. 'X', 'O', or ' ')
 │ + getSymbol(): char
 │ + setSymbol(symbol: char): void
 └─────────────┘

 ┌─────────────┐
 │    Move     │
 ├─────────────┤
 │ - player: Player
 │ - row: int
 │ - col: int
 │ - symbol: char
 │ + getPlayer(): Player
 │ + getRow(): int
 │ + getCol(): int
 │ + getSymbol(): char
 └─────────────┘
 */

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

class Game {
    private Board board;
    private List<Player> players;
    private int currentPlayerIndex;
    private boolean gameOver;

    public Game(List<Player> players, int boardSize) {
        this.players = players;
        this.board = new Board(boardSize);
        this.currentPlayerIndex = 0;
        this.gameOver = false;
    }

    public void startGame() {
        board.initializeBoard();
        while (!gameOver) {
            playTurn();
        }
    }

    private void playTurn() {
        Player currentPlayer = players.get(currentPlayerIndex);

        // Acquire a move (for human, prompt input; for AI, compute move).
        Move move = currentPlayer.getMove(board);

        // Validate move.
        if (board.isValidMove(move.getRow(), move.getCol())) {
            board.placeMark(move);

            // Check win condition.
            if (board.checkWin(move)) {
                System.out.println("Player " + currentPlayer.getSymbol() + " wins!");
                gameOver = true;
            } else if (board.isBoardFull()) {
                System.out.println("It's a draw!");
                gameOver = true;
            } else {
                // Switch to next player.
                currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
            }
        } else {
            System.out.println("Invalid move. Try again!");
        }
    }
}

 class Board {
    private Cell[][] grid;
    private int size;

    public Board(int size) {
        this.size = size;
        this.grid = new Cell[size][size];
    }

    public void initializeBoard() {
        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                grid[row][col] = new Cell(row, col);
            }
        }
    }

    public boolean isValidMove(int row, int col) {
        if (row < 0 || row >= size || col < 0 || col >= size) {
            return false;
        }
        return grid[row][col].getSymbol() == ' ';
    }

    public void placeMark(Move move) {
        grid[move.getRow()][move.getCol()].setSymbol(move.getSymbol());
    }

    public boolean checkWin(Move move) {
        // Check the row, column, and possibly diagonals
        // for the player’s symbol. If all cells match,
        // this is a win.

        char symbol = move.getSymbol();
        int row = move.getRow();
        int col = move.getCol();

        // 1. Check row
        boolean rowWin = true;
        for (int c = 0; c < size; c++) {
            if (grid[row][c].getSymbol() != symbol) {
                rowWin = false;
                break;
            }
        }
        if (rowWin) return true;

        // 2. Check column
        boolean colWin = true;
        for (int r = 0; r < size; r++) {
            if (grid[r][col].getSymbol() != symbol) {
                colWin = false;
                break;
            }
        }
        if (colWin) return true;

        // 3. Check main diagonal
        if (row == col) {
            boolean diagWin = true;
            for (int i = 0; i < size; i++) {
                if (grid[i][i].getSymbol() != symbol) {
                    diagWin = false;
                    break;
                }
            }
            if (diagWin) return true;
        }

        // 4. Check anti-diagonal
        if (row + col == size - 1) {
            boolean antiDiagWin = true;
            for (int i = 0; i < size; i++) {
                if (grid[i][(size - 1) - i].getSymbol() != symbol) {
                    antiDiagWin = false;
                    break;
                }
            }
            if (antiDiagWin) return true;
        }

        return false;
    }

    public boolean isBoardFull() {
        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                if (grid[row][col].getSymbol() == ' ') {
                    return false;
                }
            }
        }
        return true;
    }
}


class Cell {
    private int row;
    private int col;
    private char symbol;

    public Cell(int row, int col) {
        this.row = row;
        this.col = col;
        this.symbol = ' '; // empty by default
    }

    public char getSymbol() {
        return symbol;
    }

    public void setSymbol(char symbol) {
        this.symbol = symbol;
    }

    // Additional getters if needed
    public int getRow() {
        return row;
    }
    public int getCol() {
        return col;
    }
}



class Player {
    private char symbol;
    private Scanner scanner;

    public Player(char symbol) {
        this.symbol = symbol;
        this.scanner = new Scanner(System.in);
    }

    public char getSymbol() {
        return symbol;
    }

    public Move getMove(Board board) {
        System.out.println("Player " + symbol + ", enter your move (row col): ");
        int row = scanner.nextInt();
        int col = scanner.nextInt();
        
        return new Move(this, row, col, symbol);
    }
}

class Move {
    private Player player;
    private int row;
    private int col;
    private char symbol;

    public Move(Player player, int row, int col, char symbol) {
        this.player = player;
        this.row = row;
        this.col = col;
        this.symbol = symbol;
    }

    public Player getPlayer() {
        return player;
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    public char getSymbol() {
        return symbol;
    }
}

public class TicTacToe {
    public static void main(String[] args) {
        Player playerX = new Player('X');
        Player playerO = new Player('O');

        List<Player> players = new ArrayList<>();
        players.add(playerX);
        players.add(playerO);

        Game game = new Game(players, 3);
        game.startGame();
    }
}




/*
public interface IPlayer {
    char getSymbol();
    Move getMove(Board board);
}
public abstract class Player {
    protected char symbol;

    public Player(char symbol) {
        this.symbol = symbol;
    }

    public char getSymbol() {
        return symbol;
    }

    public abstract Move getMove(Board board);
}

import java.util.Scanner;

public class HumanPlayer extends Player {
    private Scanner scanner;

    public HumanPlayer(char symbol) {
        super(symbol);
        this.scanner = new Scanner(System.in);
    }

    @Override
    public Move getMove(Board board) {
        System.out.println("Player " + symbol + ", enter your move (row col): ");
        int row = scanner.nextInt();
        int col = scanner.nextInt();
        return new Move(this, row, col, symbol);
    }
}
    
import java.util.Random;

public class AIPlayer extends Player {
    private Random random;

    public AIPlayer(char symbol) {
        super(symbol);
        this.random = new Random();
    }

    @Override
    public Move getMove(Board board) {
        // Simple approach: keep trying random cells until we find an empty one.
        int size = board.getSize();
        int row, col;

        do {
            row = random.nextInt(size);
            col = random.nextInt(size);
        } while (!board.isValidMove(row, col));

        System.out.println("AI " + symbol + " chooses [" + row + ", " + col + "]");
        return new Move(this, row, col, symbol);
    }
}

 */