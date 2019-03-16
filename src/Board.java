import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;

public class Board {
    public static final byte EMPTY = 'E', WHITE = 'W', BLACK = 'B';
    public static final int MAX_SIZE = 9; // vertically and horizontally

    public static class Coordinates {
        public final int x;
        public final int y;

        Coordinates(int x, int y) {
            this.x = x;
            this.y = y;
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
}
