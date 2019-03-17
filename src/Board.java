import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;

import java.util.ArrayList;
import java.util.List;

public class Board {
    public static final byte EMPTY = 'E', WHITE = 'W', BLACK = 'B';
    public static final int MAX_SIZE = 9; // vertically and horizontally

    public static class Coordinate {
        public final int x;
        public final int y;

        Coordinate(int x, int y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public String toString() {
            return "(" + x + "," + y + ")";
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof Coordinate))
                return false;

            Coordinate other = (Coordinate) o;
            return x == other.x && y == other.y;
        }
    }

    public static class Move {
        public final Coordinate from;
        public final BoardUtil.Neighbor to;
        private final Board board;

        Move(Board context, Coordinate from, BoardUtil.Neighbor to) {
            this.from = from;
            this.to = to;
            board = context;
        }

        public boolean isLegal() {
            // TODO side move
            byte[][] repr = board.representation();
            if (repr[from.y][from.x] != board.currentPlayer()) {
                // Logic error. TODO throw error or remove this
                return false;
            }
            int playerMarbleCnt = 1;
            int opponentMarbleCnt = 0;

            BoardUtil.Neighbor next = to;
            while (repr[next.coordinate.y][next.coordinate.x] == board.currentPlayer()) {
                playerMarbleCnt += 1;
                next = next.neighbors().fromDirection(to.direction);
                if (playerMarbleCnt == 4 || next == null)
                    return false;
            }

            while (repr[next.coordinate.y][next.coordinate.x] == board.currentOpponent()) {
                opponentMarbleCnt += 1;
                next = next.neighbors().fromDirection(to.direction);
                if (next == null)
                    return playerMarbleCnt > opponentMarbleCnt;
                if (playerMarbleCnt <= opponentMarbleCnt)
                    return false;
            }

            return repr[next.coordinate.y][next.coordinate.x] == Board.EMPTY;
        }
    }

    private byte[][] board;
    private Cell[][] cells;
    private Pane pane;

    Board(byte[][] board, double height) {
        this.board = board;
        pane = new Pane();

        double width = height / Math.sin(Math.PI / 3);
        Polygon background = Hexagon.drawable(width / 2, height / 2 , width / 2, 0);
        background.setFill(Color.DEEPSKYBLUE);
        pane.getChildren().add(background);

        double yPadding = height / (MAX_SIZE + 5); // somewhat relative to cell height but mostly arbitrary
        double innerBoardHeight = height - yPadding * 2;
        double cellHeight = innerBoardHeight / 7; // Counting vertically: cell height * 5 + hex side * 4, where cell height = 2 * hex side
        double xPadding = (width - Hexagon.width30Deg(cellHeight / 2) * 9) / 2;

        initCells(board, cellHeight, xPadding, yPadding);
        setupBoard(board);
    }

    private void initCells(byte[][] board, double cellHeight, double xOffset, double yOffset) {
        cells = new Cell[board.length][];
        for (int row = 0; row < board.length; ++row) {
            cells[row] = new Cell[board[row].length];
        }

        double cellWidth = Hexagon.width30Deg(cellHeight / 2);
        double verticalHexOffset = Hexagon.verticalOffset30Deg(cellHeight / 2);
        double xWidthOffset = -(cellHeight - cellWidth) / 2;

        for (int row = 0; row < cells.length; ++row) {
            for (int col = 0; col < cells[row].length; ++col) {
                double x = cellWidth * (col + (MAX_SIZE- cells[row].length) / 2.0) + xWidthOffset + xOffset;
                double y = row * (cellHeight - verticalHexOffset) + yOffset;
                Cell c = new Cell(cellHeight, x, y);
                cells[row][col] = c;
                pane.getChildren().add(c);
            }
        }
    }

    public List<Move> inlineLegalMoves(Coordinate from) {
        List<Move> moves = new ArrayList<>();
        BoardUtil.Neighbors neighbors = BoardUtil.neighborsOf(from);
        for (BoardUtil.Neighbor to : neighbors.toArray()) {
            if (to != null) {
                Move move = new Move(this, from, to);
                if (move.isLegal()) {
                    moves.add(move);
                }
            }
        }
        return moves;
    }

    private void setupBoard(byte[][] board) {
        for (int row = 0; row < board.length; ++row) {
            for (int col = 0; col < board[row].length; ++col) {
                Cell c = boardCell(col, row);
                if (board[row][col] == Board.EMPTY)
                    c.setEmpty();
                else
                    c.setMarble(new Marble(board[row][col]));
            }
        }
    }

    private Cell boardCell(int x, int y) {
        return cells[y][x];
    }

    public Node drawable() {
        return pane;
    }

    public byte[][] representation() {
        return board;
    }

    public byte currentPlayer() {
        // TODO
        return Board.BLACK;
    }

    public byte currentOpponent() {
        // TODO
        return Board.WHITE;
    }
}
