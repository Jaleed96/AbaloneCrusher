import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;

public class Board {
    public static final byte EMPTY = 'E', WHITE = 'W', BLACK = 'B';
    public static final int MAX_SIZE = 9; // vertically and horizontally
    public static final int SCORE_TO_WIN = 6;

    interface ScoreUpdateListener {
        void scoreUpdate(Player player, Marble pushedOff, boolean gameOver);
    }

    interface CurrentPlayerChangedListener {
        void onCurrentPlayerChanged(Player currentPlayer);
    }

    interface TimeUpdatedListener {
        void onTimeUpdated(Player currentPlayer, int timeLeftForPlayer);
    }

    interface PastGameStateListener {
        void onPastGameState(Gamestate gamestate, Move move);
    }

    private byte[][] board;
    private Cell[][] cells;
    private Pane pane;
    private Player current;
    private Player opponent;
    public int blackMovesLeft;
    public int whiteMovesLeft;
    public boolean GAME_STOPPED = false;
    public boolean GAME_PAUSED = false;
    private int blackTurnTimeLeft;
    private int whiteTurnTimeLeft;
    private Timer gameTimer;
    private ScoreUpdateListener scoreUpdateListener = (player, pushedOff, gameOver) -> {
    };
    private CurrentPlayerChangedListener currentPlayerChangedListener = currentPlayer -> {
    };
    private TimeUpdatedListener timeUpdatedListener = (currentPlayer, timeLeftForPlayer) -> {
    };
    private PastGameStateListener pastGameStateListener = (gamestate, move) -> {
    };

    private double width;

    Board(byte[][] board, double height, Config config) {
        this.board = board;
        pane = new Pane();

        current = new Player(Board.BLACK, config.moveLimit, config.p1timeLimit * 1000);
        opponent = new Player(Board.WHITE, config.moveLimit, config.p2timeLimit * 1000);

        blackMovesLeft = config.moveLimit;
        whiteMovesLeft = config.moveLimit;
        blackTurnTimeLeft = current.getTimeLimit();
        whiteTurnTimeLeft = opponent.getTimeLimit();

        gameTimer = new Timer();
        gameTimer.schedule(new Countdown(), 0, 10);

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
        setupMarbles(board);
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
                Cell c = new Cell(cellHeight, x, y, BoardUtil.COORDINATES[row][col]);
                cells[row][col] = c;
                pane.getChildren().add(c);
            }
        }
    }

    private void setupMarbles(byte[][] board) {
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
        Gamestate gamestate = new Gamestate(this.representation(), this.currentPlayer(), this.currentOpponent(),
                this.blackMovesLeft, this.whiteMovesLeft);
        pastGameStateListener.onPastGameState(gamestate, move);
        applyMove(move);
        nextTurn();
    }

    private void applyMove(Move move) {
        Optional<Byte> maybePushedOff = move.apply(board);
        Marble pushedOff = null;
        for (Push p : move.pushes()) {
            pushedOff = visualPushPiece(p);
        }

        final Marble pushedOffMarble = pushedOff; // make compiler happy
        maybePushedOff.ifPresent(pushedOffPiece -> {
            updateScore(pushedOffPiece, pushedOffMarble);
        });
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
        return next == null && currentMarble != null ? currentMarble : null;
    }

    private void updateScore(byte pushedOffPiece, Marble pushedOffMarble) {
        if (currentOpponent().piece == pushedOffPiece) {
            currentPlayer().increaseScore();
            scoreUpdateListener.scoreUpdate(currentPlayer(), pushedOffMarble, currentPlayer().score() == SCORE_TO_WIN);
        } else {
            System.out.println(
                    "Board::updateScore - possible logic error. Verify that you meant to increase the opponent's score.");
            currentOpponent().increaseScore();
            scoreUpdateListener.scoreUpdate(currentOpponent(), pushedOffMarble,
                    currentOpponent().score() == SCORE_TO_WIN);
        }
    }

    private void nextTurn() {
        Player t = current;
        current = opponent;
        opponent = t;
        refreshTurnData();

        currentPlayerChangedListener.onCurrentPlayerChanged(current);
    }

    private void refreshTurnData() {
        switch (currentPlayer().piece) {
        case 'W':
            blackMovesLeft--;
            whiteTurnTimeLeft = currentPlayer().getTimeLimit();
            break;
        case 'B':
            whiteMovesLeft--;
            blackTurnTimeLeft = currentPlayer().getTimeLimit();
            break;
        }
    }

    Cell[][] cells() {
        return cells;
    }

    public void setTextCoordVisibility(boolean visible) {
        for (Cell[] row : cells) {
            for (Cell c : row)
                c.setTextCoordVisibility(visible);
        }
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

    public double getWhiteTimeLeft() {
        return (whiteTurnTimeLeft);
    }

    public double getBlackTimeLeft() {
        return (blackTurnTimeLeft);
    }

    /**
     * @param board
     *            the board to set
     */
    public void setBoard(byte[][] board) {
        this.board = board;
        setupMarbles(board);
    }

    /**
     * @param current
     *            the current to set
     */
    public void setCurrent(Player current) {
        this.current = current;
    }

    /**
     * @param opponent
     *            the opponent to set
     */
    public void setOpponent(Player opponent) {
        this.opponent = opponent;
    }

    public void setScoreUpdateListener(ScoreUpdateListener listener) {
        scoreUpdateListener = listener;
    }

    public void setCurrentPlayerChangedListener(CurrentPlayerChangedListener listener) {
        currentPlayerChangedListener = listener;
    }

    public void setTimeUpdatedListener(TimeUpdatedListener listener) {
        timeUpdatedListener = listener;
    }

    public void setTurnTimeLeft(Player player) {
        if (player.piece == 'W') {
            whiteTurnTimeLeft = player.getTimeLimit();
        } else {
            blackTurnTimeLeft = player.getTimeLimit();
        }

    }

    public void setPastGameStateListener(PastGameStateListener listener) {
        pastGameStateListener = listener;
    }

    private class Countdown extends TimerTask {
        @Override
        public void run() {
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    if (!GAME_PAUSED) {
                        switch (currentPlayer().piece) {
                        case 'W':
                            whiteTurnTimeLeft -= 10;
                            timeUpdatedListener.onTimeUpdated(current, whiteTurnTimeLeft);
                            break;
                        case 'B':
                            blackTurnTimeLeft -= 10;
                            timeUpdatedListener.onTimeUpdated(current, blackTurnTimeLeft);
                            break;
                        }
                    }
                }
            });
        }
    }
}
