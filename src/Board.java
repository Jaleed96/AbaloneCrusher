import java.util.Map;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;

import javafx.application.Platform;

/** Game logic */
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

    interface GameInSessionListener {
        void onGameStatusChange(Player winner, String winType);
    }

    private static final int TIME_STEP_MS = 10;

    private byte[][] board;
    private GuiBoard gui;
    private Player current;
    private Player opponent;
    private Player winner;
    public int blackMovesLeft;
    public int whiteMovesLeft;
    public boolean GAME_STOPPED = false;
    public boolean GAME_PAUSED = false;
    private int blackTurnTimeLeft;
    private int whiteTurnTimeLeft;
    private Timer gameTimer;
    private ScoreUpdateListener scoreUpdateListener = (player, pushedOff, gameOver) -> { };
    private CurrentPlayerChangedListener currentPlayerChangedListener = currentPlayer -> { };
    private TimeUpdatedListener timeUpdatedListener = (currentPlayer, timeLeftForPlayer) -> { };
    private PastGameStateListener pastGameStateListener = (gamestate, move) -> { };
    private GameInSessionListener gameInSessionListener = (winner, winType) -> { };

    Board(byte[][] board, double height, Config config) {
        this.board = BoardUtil.deepCopyRepresentation(board);
        gui = new GuiBoard(board, height);

        current = new Player(Board.BLACK, config.moveLimit, config.blackTimeLimitMs);
        opponent = new Player(Board.WHITE, config.moveLimit, config.whiteTimeLimitMs);

        blackMovesLeft = config.moveLimit;
        whiteMovesLeft = config.moveLimit;
        blackTurnTimeLeft = current.getTimeLimitMs();
        whiteTurnTimeLeft = opponent.getTimeLimitMs();

        gameTimer = new Timer();
        gameTimer.schedule(new Countdown(), 0, TIME_STEP_MS);
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
            pushedOff = gui.visualPushPiece(p);
        }

        final Marble pushedOffMarble = pushedOff; // make compiler happy
        maybePushedOff.ifPresent(pushedOffPiece -> {
            updateScore(pushedOffPiece, pushedOffMarble);
        });
    }

    private void updateScore(byte pushedOffPiece, Marble pushedOffMarble) {
        if (currentOpponent().piece == pushedOffPiece) {
            currentPlayer().increaseScore();
            if (currentPlayer().score() == SCORE_TO_WIN) endGameSession(current, "Pushed 6 marbles off board");
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
        case Board.WHITE:
            if (blackMovesLeft>1) {
                blackMovesLeft--;
                whiteTurnTimeLeft = currentPlayer().getTimeLimitMs();
            } else {
                blackMovesLeft--;
                endGameSession(current, "B has 0 moves left");
            }
            break;
        case Board.BLACK:
            if (whiteMovesLeft>1) {
                whiteMovesLeft--;
                blackTurnTimeLeft = currentPlayer().getTimeLimitMs();
            } else {
                whiteMovesLeft--;
                endGameSession(current, "W has 0 moves left");
            }
            break;
        }
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

    public double getWhiteTimeLeft() {
        return (whiteTurnTimeLeft);
    }

    public double getBlackTimeLeft() {
        return (blackTurnTimeLeft);
    }

    public void setGamestate(Gamestate gamestate) {
        Gamestate gsCopy = new Gamestate(gamestate);

        board = gsCopy.board;
        gui.setupMarbles(board);
        current = gsCopy.currentPlayer;
        opponent = gsCopy.opponent;
        blackMovesLeft = gsCopy.movesLeftB;
        whiteMovesLeft = gsCopy.movesLeftW;
        setTurnTimeLeft(currentPlayer());
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

    public void setGameInSessionListener(GameInSessionListener listener) {
        gameInSessionListener = listener;
    }

    private void endGameSession(Player winner, String winType) {
        GAME_STOPPED = true;
        this.winner = winner;
        gameInSessionListener.onGameStatusChange(winner, winType);
    }

    public Player getWinner() { return this.winner; }

    public void setTurnTimeLeft(Player player) {
        if (player.piece == WHITE) {
            whiteTurnTimeLeft = player.getTimeLimitMs();
        } else {
            blackTurnTimeLeft = player.getTimeLimitMs();
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
                    if (!(GAME_PAUSED || GAME_STOPPED)) {
                        switch (currentPlayer().piece) {
                        case WHITE:
                            if (whiteTurnTimeLeft>0) {
                                whiteTurnTimeLeft -= TIME_STEP_MS;
                                timeUpdatedListener.onTimeUpdated(current, whiteTurnTimeLeft);
                            } else endGameSession(opponent, "Wins by Timeout");
                            break;
                        case BLACK:
                            if (blackTurnTimeLeft>0) {
                                blackTurnTimeLeft -= TIME_STEP_MS;
                                timeUpdatedListener.onTimeUpdated(current, blackTurnTimeLeft);
                            } else endGameSession(opponent, "Wins by Timeout");
                            break;
                        }
                    }
                }
            });
        }
    }

    public static byte playersOpponent(byte p) {
        switch (p) {
            case Board.WHITE: return Board.BLACK;
            case Board.BLACK: return Board.WHITE;
            default: { // This is just to make the compiler happy
                System.err.println("Board::playersOpponent received byte " + p);
                return Board.EMPTY;
            }
        }
    }

    public GuiBoard gui() {
        return gui;
    }
}
