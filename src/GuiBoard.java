import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;

public class GuiBoard extends Pane {
    private Cell[][] cells;

    GuiBoard(byte[][] initialBoardSetup, double height) {
        double width = height / Math.sin(Math.PI / 3);
        Polygon background = Hexagon.drawable(width / 2, height / 2, width / 2, 0);
        background.setFill(Color.DEEPSKYBLUE);
        getChildren().add(background);

        double yPadding = height / (Board.MAX_SIZE + 5); // somewhat relative to cell height but mostly arbitrary
        double innerBoardHeight = height - yPadding * 2;
        double cellHeight = innerBoardHeight / 7; // Counting vertically: cell height * 5 + hex side * 4, where cell
        // height = 2 * hex side
        double xPadding = (width - Hexagon.width30Deg(cellHeight / 2) * 9) / 2;

        initCells(initialBoardSetup, cellHeight, xPadding, yPadding);
        setupMarbles(initialBoardSetup);
    }

    Cell[][] cells() {
        return cells;
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
                double x = cellWidth * (col + (Board.MAX_SIZE - cells[row].length) / 2.0) + xWidthOffset + xOffset;
                double y = row * (cellHeight - verticalHexOffset) + yOffset;
                Cell c = new Cell(cellHeight, x, y, BoardUtil.COORDINATES[row][col]);
                cells[row][col] = c;
                getChildren().add(c);
            }
        }
    }

    void setupMarbles(byte[][] board) {
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

    public void setTextCoordVisibility(boolean visible) {
        for (Cell[] row : cells) {
            for (Cell c : row)
                c.setTextCoordVisibility(visible);
        }
    }

    // this function is meant to be used right after pushPiece to update gui and
    // retrieve the pushedOff marble if there's one
    Marble visualPushPiece(Push p) {
        BoardUtil.Neighbor next = p.to;
        Marble currentMarble = boardCell(p.from.x, p.from.y).removeMarble();
        while (next != null && currentMarble != null) {
            Marble nextMarble = boardCell(next.coordinate.x, next.coordinate.y).removeMarble();
            boardCell(next.coordinate.x, next.coordinate.y).setMarble(currentMarble);
            next = next.neighbors().fromDirection(next.direction);
            currentMarble = nextMarble;
        }
        return next == null && currentMarble != null ? currentMarble : null;
    }
}
