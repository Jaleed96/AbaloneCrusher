import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Board {
    public static final byte EMPTY = 'E', WHITE = 'W', BLACK = 'B';
    public static final int MAX_SIZE = 9; // vertically and horizontally

    private byte[][] board;
    private Cell[][] cells;
    private Pane pane;
    private Player current;
    private Player opponent;

    private double width;

    Board(byte[][] board, double height) {
        this.board = board;
        pane = new Pane();

        current = new Player(Board.BLACK);
        opponent = new Player(Board.WHITE);

        double width = height / Math.sin(Math.PI / 3);
        this.width = width;
        Polygon background = Hexagon.drawable(width / 2, height / 2, width / 2, 0);
        background.setFill(Color.DEEPSKYBLUE);
        pane.getChildren().add(background);

        double yPadding = height / (MAX_SIZE + 5); // somewhat relative to cell height but mostly arbitrary
        double innerBoardHeight = height - yPadding * 2;
        double cellHeight = innerBoardHeight / 7; // Counting vertically: cell height * 5 + hex side * 4, where cell
                                                  // height = 2 * hex side
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
                double x = cellWidth * (col + (MAX_SIZE - cells[row].length) / 2.0) + xWidthOffset + xOffset;
                double y = row * (cellHeight - verticalHexOffset) + yOffset;
                Cell c = new Cell(cellHeight, x, y, col, row);
                cells[row][col] = c;
                pane.getChildren().add(c);
            }
        }
    }

    public List<Move> inlineLegalMoves(Coordinate from) {
        List<Move> moves = new ArrayList<>();
        BoardUtil.Neighbors neighbors = BoardUtil.neighborsOf(from);
        for (BoardUtil.Neighbor to : neighbors.toArray()) {
            Move move = new Move(new Push(from, to));
            if (move.isLegalInline(this)) {
                moves.add(move);
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

    public void makeMove(Move move) throws Move.IllegalMoveException {
        if (!move.isLegal(this)) {
            StringBuilder erroMsg = new StringBuilder().append("Illegal move:");
            for (Push m : move.pushes()) {
                erroMsg.append(String.format(" [%s to %s, %s to %s]", m.from.toString(), m.to.coordinate.toString(),
                        BoardUtil.toConformanceCoord(m.from), BoardUtil.toConformanceCoord(m.to.coordinate)));
            }
            throw new Move.IllegalMoveException(erroMsg.toString());
        }

        applyMove(move);
        nextTurn();
    }

    private void applyMove(Move move) {
        Optional<Byte> maybePushedOff = Optional.empty();
        Marble pushedOff = null;
        for (Push p : move.pushes()) {
            maybePushedOff = pushPiece(p);
            pushedOff = visualPushPiece(p);
        }

        final Marble finalPushedOff = pushedOff; // make compiler happy
        maybePushedOff.ifPresent(pushedOffPiece -> {
            // if the logic is right, pushedOff can never be null here;
            assert finalPushedOff != null;
            updateScore(pushedOffPiece);
        });
    }

    /// Pushes the piece in the board representation only, to update gui use
    /// visualPushPiece after this
    /// assumes that the move has been validated beforehand
    private Optional<Byte> pushPiece(Push p) {
        BoardUtil.Neighbor next = p.to;
        byte currentPiece = board[p.from.y][p.from.x];
        board[p.from.y][p.from.x] = Board.EMPTY;
        while (next != null && currentPiece != Board.EMPTY) {
            byte nextPiece = board[next.coordinate.y][next.coordinate.x];
            board[next.coordinate.y][next.coordinate.x] = currentPiece;
            next = next.neighbors().fromDirection(next.direction);
            currentPiece = nextPiece;
        }
        /// If the piece has been pushed off the board, return it
        return next == null ? Optional.of(currentPiece) : Optional.empty();
    }

    // this function is meant to be used right after pushPiece to update gui and
    // retrieve the pushedOff marble if there's one
    private Marble visualPushPiece(Push p) {
        BoardUtil.Neighbor next = p.to;
        Marble currentMarble = boardCell(p.from.x, p.from.y).removeMarble();
        while (next != null && currentMarble != null) {
            Marble nextMarble = boardCell(next.coordinate.x, next.coordinate.y).removeMarble();
            boardCell(next.coordinate.x, next.coordinate.y).setMarble(currentMarble);
            next = next.neighbors().fromDirection(next.direction);
            currentMarble = nextMarble;
        }
        return next == null ? currentMarble : null;
    }

    private void updateScore(byte pushedOffPiece) {
        if (currentOpponent().piece == pushedOffPiece)
            currentPlayer().increaseScore();
        else
            currentOpponent().increaseScore();
    }

    private void nextTurn() {
        Player t = current;
        current = opponent;
        opponent = t;
    }

    Cell[][] cells() {
        return cells;
    }

    public Node drawable() {
        return pane;
    }

    public byte[][] representation() {
        return board;
    }

    public Player currentPlayer() {
        return current;
    }

    public Player currentOpponent() {
        return opponent;
    }

    public double getWidth() {
        return this.width;
    }
}
