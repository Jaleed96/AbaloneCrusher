import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

/** Minimax algorithm with alpha-beta pruning */
public class Minimax {

    public static int SAFE_TIMEOUT_THRESHOLD_MS = 200;

    interface SearchInterruptHandle {
        Move interruptWithOutput();
    }

    /** Smaller version of Gamestate class, there's some code duplication here */
    static class State {
        final byte[][] board;
        byte maximizingPlayer;
        byte minimizingPlayer;
        int movesLeftB;
        int movesLeftW;
        int scoreB;
        int scoreW;

        State(byte[][] board, byte maximizingPlayer, byte minimizingPlayer, int movesLeftB, int movesLeftW, int scoreB, int scoreW) {
            this.board = BoardUtil.deepCopyRepresentation(board);
            this.maximizingPlayer = maximizingPlayer;
            this.minimizingPlayer = minimizingPlayer;
            this.movesLeftB = movesLeftB;
            this.movesLeftW = movesLeftW;
            this.scoreB = scoreB;
            this.scoreW = scoreW;
        }
    }

    /** Scored move helps retrieving the actual move after the maximum value has been calculated */
    private static class ScoredMove {
        int val;
        Move move;

        ScoredMove(int val, Move move) {
            this.val = val;
            this.move = move;
        }
    }

    private final AtomicBoolean interruptFlag = new AtomicBoolean(false);
    private State initialSearchState;

    public SearchInterruptHandle searchBestMove(final State state) {
        interruptFlag.set(false);
        initialSearchState = state;

        ExecutorService exec = Executors.newSingleThreadExecutor();
        Future<Move> resultFuture = exec.submit(() -> {
            int depth = 1;
            Move result = null;

            while (!interruptFlag.get()) {
                ScoredMove r = topLevelMaximize(initialSearchState, depth++);
                if (r.move != null)
                    result = r.move;
            }

            return result;
        });
        exec.shutdown();

        return () -> {
            interrupt();
            try {
                return resultFuture.get();
            } catch (InterruptedException | ExecutionException e) {
                // Neither exception should ever happen unless we have some serious errors
                System.err.println("Something went terribly wrong and minimax failed to find a move.");
                e.printStackTrace();
                return null;
            }
        };
    }

    private void interrupt() {
        interruptFlag.set(true);
    }

    /** Matches moves to their scores */
    private ScoredMove topLevelMaximize(State state, int depth) {
        if (gameOver(state))
            return new ScoredMove(Heuristic.evaluate(state), null);

        ScoredMove bestMove = new ScoredMove(Integer.MIN_VALUE, null);
        List<OrderedMove> moves = MoveGenerator.generate(state.board, state.maximizingPlayer, state.minimizingPlayer);
        moves.sort(OrderedMove::compareTo);
        for (OrderedMove m : moves) {
            int minVal = minimize(moveResult(state, m.move, state.maximizingPlayer), bestMove.val, Integer.MAX_VALUE, depth - 1);
            System.out.println(minVal + " " + MoveParser.toText(m.move) + " " + m.type);
            if (interruptFlag.get()) {
                // if interruptFlag is set, value returned by minimize likely doesn't make sense
                break;
            }
            if (minVal > bestMove.val) {
                bestMove.val = minVal;
                bestMove.move = m.move;
            }
        }

        return bestMove;
    }

    private int maximize(State state, int alpha, int beta, int depth) {
        if (interruptFlag.get()) {
            return alpha;
        }

        if (gameOver(state) || depth == 0)
            return Heuristic.evaluate(state);

        int val = Integer.MIN_VALUE;
        List<OrderedMove> moves = MoveGenerator.generate(state.board, state.maximizingPlayer, state.minimizingPlayer);
        moves.sort(OrderedMove::compareTo);
        for (OrderedMove m : moves) {
            val = Math.max(val, minimize(moveResult(state, m.move, state.maximizingPlayer), alpha, beta, depth - 1));
            if (val >= beta) return val;
            alpha = Math.max(alpha, val);
        }

        return val;
    }

    private int minimize(State state, int alpha, int beta, int depth) {
        if (interruptFlag.get()) {
            return beta;
        }

        if (gameOver(state) || depth == 0)
            return Heuristic.evaluate(state);

        int val = Integer.MAX_VALUE;
        List<OrderedMove> moves = MoveGenerator.generate(state.board, state.minimizingPlayer, state.maximizingPlayer);
        moves.sort(OrderedMove::compareTo);
        for (OrderedMove m : moves) {
            val = Math.min(val, maximize(moveResult(state, m.move, state.minimizingPlayer), alpha, beta, depth - 1));
            if (val <= alpha) return val;
            beta = Math.min(beta, val);
        }

        return val;
    }

    private static boolean gameOver(State state) {
        return state.movesLeftB == 0 && state.movesLeftW == 0
                || state.scoreB == Board.SCORE_TO_WIN
                || state.scoreW == Board.SCORE_TO_WIN;
    }

    private static State moveResult(State state, Move move, byte movingPlayer) {
        final State newState = new State(state.board, state.maximizingPlayer, state.minimizingPlayer, state.movesLeftB, state.movesLeftW, state.scoreB, state.scoreW);
        Optional<Byte>[] scoreUpdates = move.apply(newState.board);
        for (Optional<Byte> maybeScore : scoreUpdates) {
            maybeScore.ifPresent(piece -> {
                if (piece == Board.WHITE)
                    newState.scoreB += 1;
                else if (piece == Board.BLACK)
                    newState.scoreW += 1;
            });
        }

        if (movingPlayer == Board.WHITE)
            newState.movesLeftW -= 1;
        else if (movingPlayer == Board.BLACK)
            newState.movesLeftB -= 1;

        return newState;
    }
}