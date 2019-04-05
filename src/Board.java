import javafx.application.Platform;

import java.util.List;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CopyOnWriteArrayList;

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
    private Minimax minimax;
    private ScoreUpdateListener scoreUpdateListener = (blackPlayer, whitePlayer) -> { };
    private CurrentPlayerChangedListener currentPlayerChangedListener = currentPlayer -> { };
    private List<TimeUpdatedListener> timeUpdatedListeners = new CopyOnWriteArrayList<>();
    private PastGameStateListener pastGameStateListener = (gamestate, move) -> { };
    private GameInSessionListener gameInSessionListener = (winner, winType) -> { };

    // ai stuff
    private TimeUpdatedListener aiTimeoutHandler = (currentPlayer, timeLeftForPlayer) -> { };
    private Minimax.SearchInterruptHandle lastSearchHandle = () -> null;


    Board(byte[][] board, double height, Config config) {
        this.board = BoardUtil.deepCopyRepresentation(board);
        gui = new GuiBoard(board, height);

        current = new Player(config.blackAgent, Board.BLACK, config.moveLimit, config.blackTimeLimitMs, new PrimaryHeuristic());
        opponent = new Player(config.whiteAgent, Board.WHITE, config.moveLimit, config.whiteTimeLimitMs, new PrimaryHeuristic());

        blackMovesLeft = config.moveLimit;
        whiteMovesLeft = config.moveLimit;
        curPlayerTurnTimeLeft = current.getTimeLimitMs();

        gameTimer = new Timer(true);
        gameTimer.schedule(new Countdown(), 0, TIME_STEP_MS);
        minimax = new Minimax();
    }

    private void runAI(){
        if (!GAME_STOPPED && current.agent == Config.PlayerAgent.AI) {
            lastSearchHandle = minimax.searchBestMove(
                    // current is the maximizing player, opponent is the minimizing player
                    new Minimax.State(board, current.piece, opponent.piece, blackMovesLeft, whiteMovesLeft, current.score(), opponent.score()),
                    current.heuristic()
            );

            aiTimeoutHandler = (currentPlayer, timeLeftForPlayer) -> {
                if (timeLeftForPlayer < Minimax.SAFE_TIMEOUT_THRESHOLD_MS) {
                    Move m = lastSearchHandle.interruptWithOutput();
                    try {
                        makeMove(m);
                    } catch (Move.IllegalMoveException ignored) {
                    }
                    // The if is needed because runAI is called inside makeMove
                    if (current.agent != Config.PlayerAgent.AI) {
                        aiTimeoutHandler = (c, t) -> { };
                        lastSearchHandle = () -> null;
                    }
                }
            };
        }
    }

    private void stopAI() {
        aiTimeoutHandler = (currentPlayer, timeLeftForPlayer) -> { };
        lastSearchHandle.interruptWithOutput();
        lastSearchHandle = () -> null;
    }

    private boolean enoughMovesLeft() {
        if (opponent.piece == Board.BLACK) {
            if (whiteMovesLeft < 1) {
                return false;
            }
        } else if (opponent.piece == Board.WHITE) {
            if (blackMovesLeft < 1) {
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
        Gamestate gamestate = new Gamestate(this.representation(), this.currentPlayer(), this.currentOpponent(),
                this.blackMovesLeft, this.whiteMovesLeft);
        pastGameStateListener.onPastGameState(gamestate, move);
        applyMove(move);
        nextTurn();
        runAI();
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
            --blackMovesLeft;
            if (enoughMovesLeft()) {
                curPlayerTurnTimeLeft = currentPlayer().getTimeLimitMs();
            } else {
                endGameSession();
            }
            break;
        case Board.BLACK:
            --whiteMovesLeft;
            if (enoughMovesLeft()) {
                curPlayerTurnTimeLeft = currentPlayer().getTimeLimitMs();
            } else {
                endGameSession();
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

    public int getCurPlayerTurnTimeLeft() {
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

        stopAI();
        runAI();
    }

    public void onStop() {
        stopAI();
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

    public void addTimeUpdatedListener(TimeUpdatedListener listener) {
        timeUpdatedListeners.add(listener);
    }

    public void removeTimeUpdatedListener(TimeUpdatedListener listener) {
        timeUpdatedListeners.remove(listener);
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
        if (currentPlayer().score()>currentOpponent().score()) {
            this.winner = currentPlayer();
        } else if (currentOpponent().score()>currentPlayer().score()) {
            this.winner = currentOpponent();
        }
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
                            for (TimeUpdatedListener listen : timeUpdatedListeners) {
                                listen.onTimeUpdated(current, curPlayerTurnTimeLeft);
                            }
                            aiTimeoutHandler.onTimeUpdated(current, curPlayerTurnTimeLeft);
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

    public void doFirstRandMove(){
        if (current.agent == Config.PlayerAgent.AI) {
            try {
                makeMove(MoveGenerator.firstRandMove(board));
            } catch (Move.IllegalMoveException e) {
                System.out.println("FIRST MOVE WAS ILLEGAL?! BUT WHY??");
                e.printStackTrace();
            }
        }
    }

    public GuiBoard gui() {
        return gui;
    }
}
