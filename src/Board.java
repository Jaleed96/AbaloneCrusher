import javafx.scene.Node;
import javafx.scene.layout.Pane;

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
    private Pane pane;

    Board(byte[][] board, double cellHeight) {
        this.board = board;
        pane = new Pane();

        double cellWidth = Hexagon.width30Deg(cellHeight / 2);
        double verticalHexOffset = Hexagon.verticalOffset30Deg(cellHeight / 2);

        for (int row = 0; row < board.length; ++row) {
            for (int col = 0; col < board[row].length; ++col) {
                double x = cellWidth * (col + (MAX_SIZE - board[row].length) / 2.0);
                double y = row * (cellHeight - verticalHexOffset);
                Cell c = new Cell(cellHeight, x, y);
                if (board[row][col] != Board.EMPTY) {
                    c.setMarble(new Marble(board[row][col]));
                }
                pane.getChildren().add(c);
            }
        }
    }

    public Node drawable() {
        return pane;
    }
}
