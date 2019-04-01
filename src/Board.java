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
        void scoreUpdate(Player black, Player white);
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
    private int curPlayerTurnTimeLeft;
    private Timer gameTimer;
    private ScoreUpdateListener scoreUpdateListener = (blackPlayer, whitePlayer) -> { };
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
        curPlayerTurnTimeLeft = current.getTimeLimitMs();

        gameTimer = new Timer(true);
        gameTimer.schedule(new Countdown(), 0, TIME_STEP_MS);
    }

    private boolean enoughMovesLeft() {
        if (current.piece == Board.BLACK) {
            if (blackMovesLeft == 0) {
                return false;
            }
        } else if (current.piece == Board.WHITE) {
            if (whiteMovesLeft == 0) {
                return false;
            }
        }
        return true;

    }

    public void makeMove(Move move) throws Move.IllegalMoveException {
        if (!move.isLegal(this)) {
            StringBuilder erroMsg = new StringBuilder().append("Illegal move:");
            for (Push m : move.pushes()) {
                String toString = m.to == null ? "EDGE" : m.to.coordinate.toString();
                String conformenceString = m.to == null ? "EDGE" : BoardUtil.toConformanceCoord(m.to.coordinate);
                erroMsg.append(String.format(" [%s to %s, %s to %s]", m.from.toString(), toString,
                        BoardUtil.toConformanceCoord(m.from), conformenceString));
            }
            throw new Move.IllegalMoveException(erroMsg.toString());
        }
        if (!enoughMovesLeft()) endGameSession();
        else {
            Gamestate gamestate = new Gamestate(this.representation(), this.currentPlayer(), this.currentOpponent(),
                    this.blackMovesLeft, this.whiteMovesLeft);
            pastGameStateListener.onPastGameState(gamestate, move);
            applyMove(move);
            nextTurn();
        }
    }

    private void applyMove(Move move) {
        Optional<Byte>[] maybePushedOff = move.apply(board);
        for (Push p : move.pushes()) {
            gui.visualPushPiece(p);
        }

        for (Optional<Byte> maybeScore : maybePushedOff) {
            maybeScore.ifPresent(this::updateScore);
        }
    }

    private void updateScore(byte pushedOffPiece) {
        if (currentOpponent().piece == pushedOffPiece) {
            if (currentPlayer().increaseScore() == SCORE_TO_WIN)
                endGameSession(current, "Pushed 6 marbles off board");
        } else {
            if (currentOpponent().increaseScore() == SCORE_TO_WIN)
                endGameSession(opponent, "Pushed 6 marbles off board");
        }
        scoreUpdateCallback();
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
            blackMovesLeft--;
            curPlayerTurnTimeLeft = currentPlayer().getTimeLimitMs();
            break;
        case Board.BLACK:
            whiteMovesLeft--;
            curPlayerTurnTimeLeft = currentPlayer().getTimeLimitMs();
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

    public double getCurPlayerTurnTimeLeft() {
        return curPlayerTurnTimeLeft;
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
        scoreUpdateCallback();
    }

    private void scoreUpdateCallback() {
        if (currentPlayer().piece == BLACK) {
            scoreUpdateListener.scoreUpdate(currentPlayer(), currentOpponent());
        } else {
            scoreUpdateListener.scoreUpdate(currentOpponent(), currentPlayer());
        }
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

    private void endGameSession() {
        GAME_STOPPED = true;
        this.winner = currentPlayer().score()>currentOpponent().score() ? currentPlayer() : currentOpponent();
        gameInSessionListener.onGameStatusChange(this.winner, "Higher score of two");
    }

    public Player getWinner() {
        return this.winner;
    }

    public void setTurnTimeLeft(Player player) {
        curPlayerTurnTimeLeft = player.getTimeLimitMs();
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
                        if (curPlayerTurnTimeLeft > 0) {
                            curPlayerTurnTimeLeft -= TIME_STEP_MS;
                            timeUpdatedListener.onTimeUpdated(current, curPlayerTurnTimeLeft);
                        } else
                            endGameSession(opponent, "Wins by Timeout");
                    }
                }
            });
        }
    }

    public static byte playersOpponent(byte p) {
        switch (p) {
        case Board.WHITE:
            return Board.BLACK;
        case Board.BLACK:
            return Board.WHITE;
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
