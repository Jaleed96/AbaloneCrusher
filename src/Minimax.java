import java.util.ArrayList;
import java.util.Collections;
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
    private static int Q_SEARCH_DEPTH = 2;

    interface SearchInterruptHandle {
        Move interruptWithOutput();
        Optional<Move> getOutputIfReady();
    }

    /** Smaller version of Gamestate class, there's some code duplication here */
    static class State {
        final byte[][] board;
        byte maximizingPlayer;
        byte minimizingPlayer;
        int movesLeftB;
        int movesLeftW;
        int maxPlayerScore;
        int minPlayerScore;

        State(byte[][] board, byte maximizingPlayer, byte minimizingPlayer, int movesLeftB, int movesLeftW, int maxPlayerScore, int minPlayerScore) {
            this.board = BoardUtil.deepCopyRepresentation(board);
            this.maximizingPlayer = maximizingPlayer;
            this.minimizingPlayer = minimizingPlayer;
            this.movesLeftB = movesLeftB;
            this.movesLeftW = movesLeftW;
            this.maxPlayerScore = maxPlayerScore;
            this.minPlayerScore = minPlayerScore;
        }

        State(State toCopy) {
            this.board = BoardUtil.deepCopyRepresentation(toCopy.board);
            this.maximizingPlayer = toCopy.maximizingPlayer;
            this.minimizingPlayer = toCopy.minimizingPlayer;
            this.movesLeftB = toCopy.movesLeftB;
            this.movesLeftW = toCopy.movesLeftW;
            this.maxPlayerScore = toCopy.maxPlayerScore;
            this.minPlayerScore = toCopy.minPlayerScore;
        }
    }

    /** Scored move helps retrieving the actual move after the maximum value has been calculated */
    private static class ScoredMove implements Comparable {
        int val;
        OrderedMove move;

        ScoredMove(int val, OrderedMove move) {
            this.val = val;
            this.move = move;
        }

        @Override
        public int compareTo(Object o) {
            ScoredMove other = (ScoredMove) o;
            return Integer.compare(this.val, other.val);
        }
    }

    private final AtomicBoolean interruptFlag = new AtomicBoolean(false);
    private State initialSearchState;
    private Heuristic heuristic;

    public SearchInterruptHandle searchBestMove(final State state, Heuristic heuristic, boolean useIterativeDeepening) {
        interruptFlag.set(false);
        initialSearchState = state;
        this.heuristic = heuristic;

        ExecutorService exec = Executors.newSingleThreadExecutor();
        Future<Move> resultFuture;
        if (useIterativeDeepening) {
            resultFuture = exec.submit(() -> {
                int depth = 1;
                ScoredMove result = new ScoredMove(Integer.MIN_VALUE, null);
                ScoredMove latestDecision = new ScoredMove(Integer.MIN_VALUE, null);
                List<ScoredMove> sortedPreviousScores = null;

                while (true) {
                    sortedPreviousScores = topLevelMaximize(initialSearchState, sortedPreviousScores, depth++);
                    if (!sortedPreviousScores.isEmpty()) {
                        latestDecision = sortedPreviousScores.get(0);
                    }

                    if (!interruptFlag.get()) {
                        // always take the last depth decision
                        result = latestDecision;
                    } else {
                        break;
                    }
                }
                TranspositionTable.clear();
                // if we were interrupted, means we're at the last possible depth and we can use the move found there
                if (latestDecision.val > result.val)
                    result = latestDecision;

                return result.move.move;
            });
        } else {
            resultFuture = exec.submit(() -> {
                // 1 depth run to pre-order moves by heuristic and to ensure that a move is found
                List<ScoredMove> sortedPreviousScores = topLevelMaximize(initialSearchState, null, 1);
                ScoredMove result = sortedPreviousScores.get(0);

                sortedPreviousScores = topLevelMaximize(initialSearchState, null, 4);
                if (!sortedPreviousScores.isEmpty()) {
                    result = sortedPreviousScores.get(0);
                }

                TranspositionTable.clear();
                return result.move.move;
            });
        }
        exec.shutdown();

        return new SearchInterruptHandle() {
            @Override
            public Move interruptWithOutput() {
                interrupt();
                try {
                    return resultFuture.get();
                } catch (InterruptedException | ExecutionException e) {
                    System.err.println("Something went terribly wrong and minimax failed to find a move.");
                    e.printStackTrace();
                    return null;
                }
            }

            @Override
            public Optional<Move> getOutputIfReady() {
                if (resultFuture.isDone()) {
                    try {
                        return Optional.of(resultFuture.get());
                    } catch (InterruptedException | ExecutionException e) {
                        System.err.println("Something went terribly wrong and minimax failed to find a move.");
                        e.printStackTrace();
                    }
                }
                return Optional.empty();
            }
        };
    }

    private void interrupt() {
        interruptFlag.set(true);
    }

    /** Matches moves to their scores
     * @return sorted list of legal moves matched to their scores for this iteration */
    private List<ScoredMove> topLevelMaximize(State state, List<ScoredMove> sortedPreviousScores, int depth) {
        if (gameOver(state))
            return new ArrayList<>();

        List<OrderedMove> moves;
        if (sortedPreviousScores != null) {
            moves = new ArrayList<>(sortedPreviousScores.size());
            for (ScoredMove sm : sortedPreviousScores) {
                moves.add(sm.move);
            }
        } else {
            moves = MoveGenerator.generate(state.board, state.maximizingPlayer, state.minimizingPlayer);
            moves.sort(OrderedMove::compareTo);
        }

        int alpha = Integer.MIN_VALUE;
        List<ScoredMove> recordedMoves = new ArrayList<>();
        for (OrderedMove m : moves) {
            int minVal = minimize(moveResult(state, m.move, state.maximizingPlayer), alpha, Integer.MAX_VALUE, qSearchDepth(m, depth), depth - 1);
            //System.out.println(minVal + " " + MoveParser.toText(m.move) + " " + m.type);
            if (interruptFlag.get()) {
                // if interruptFlag is set, value returned by minimize likely doesn't make sense
                break;
            }
            recordedMoves.add(new ScoredMove(minVal, m));
            alpha = Math.max(alpha, minVal);
        }

        // put moves with highest previously found score at the front
        recordedMoves.sort(Collections.reverseOrder());

        return recordedMoves;
    }

    private int maximize(State state, int alpha, int beta, final int q, int depth) {
        if (interruptFlag.get()) {
            return alpha;
        }

        if (gameOver(state) || depth + q == 0)
            return heuristic.evaluate(state);

        TableEntry entry = TranspositionTable.get(state.board, state.maximizingPlayer);
        if (entry != null && depth+q<=entry.getDepth()) {
            int score = entry.fetchHeuristic();
            TableEntry.ScoreType type = entry.getScoreType();
            switch(type) {
                case LOWER_BOUND:
                    if (alpha<score) {
                        alpha = score;
                    }
                    break;
                case UPPER_BOUND:
                    if (beta>score) {
                        beta = score;
                    }
                    break;
                case EXACT_SCORE:
                    return score;
            }
            if (alpha>=beta) {
                return score;
            }
        }

        int val = Integer.MIN_VALUE;
        List<OrderedMove> moves = MoveGenerator.generate(state.board, state.maximizingPlayer, state.minimizingPlayer);
        moves.sort(OrderedMove::compareTo);
        for (OrderedMove m : moves) {
            val = Math.max(val, minimize(moveResult(state, m.move, state.maximizingPlayer), alpha, beta, Math.max(qSearchDepth(m, depth), q), depth - 1));
            if (val >= beta) break/*return val*/;
            alpha = Math.max(alpha, val);
        }
        if (!interruptFlag.get()) {
            TranspositionTable.put(state.board, state.maximizingPlayer, new TableEntry(val, alpha, beta, depth+q));
        }

        return val;
    }

    private int minimize(State state, int alpha, int beta, final int q, int depth) {
        if (interruptFlag.get()) {
            return beta;
        }

        if (gameOver(state) || depth + q == 0)
            return heuristic.evaluate(state);

        TableEntry entry = TranspositionTable.get(state.board, state.maximizingPlayer);
        if (entry != null && depth+q<=entry.getDepth()) {
            int score = entry.fetchHeuristic();
            TableEntry.ScoreType type = entry.getScoreType();
            switch(type) {
                case LOWER_BOUND:
                    if (alpha<score) {
                        alpha = score;
                    }
                    break;
                case UPPER_BOUND:
                    if (beta>score) {
                        beta = score;
                    }
                    break;
                case EXACT_SCORE:
                    return score;
            }
            if (alpha>=beta) {
                return score;
            }
        }

        int val = Integer.MAX_VALUE;
        List<OrderedMove> moves = MoveGenerator.generate(state.board, state.minimizingPlayer, state.maximizingPlayer);
        moves.sort(OrderedMove::compareTo);
        for (OrderedMove m : moves) {
            val = Math.min(val, maximize(moveResult(state, m.move, state.minimizingPlayer), alpha, beta, Math.max(qSearchDepth(m, depth), q), depth - 1));
            if (val <= alpha) break/*return val**/;
            beta = Math.min(beta, val);
        }

        if (!interruptFlag.get()) {
            TranspositionTable.put(state.board, state.maximizingPlayer, new TableEntry(val, alpha, beta, depth+q));
        }

        return val;
    }

    private static int qSearchDepth(OrderedMove m, int depth) {
        // We only care about q if the move that leads to a leaf is a capturing one
        if (depth != 1)
            return 0;

        switch (m.type) {
            case THREE_PUSH_TWO_CAPTURE:
            case THREE_PUSH_ONE_CAPTURE:
            case TWO_PUSH_ONE_CAPTURE:
                return Q_SEARCH_DEPTH;
            default:
                return 0;
        }
    }

    static boolean gameOver(State state) {
        return state.movesLeftB == 0 && state.movesLeftW == 0
                || state.maxPlayerScore == Board.SCORE_TO_WIN
                || state.minPlayerScore == Board.SCORE_TO_WIN;
    }

    private static State moveResult(State state, Move move, byte movingPlayer) {
        final State newState = new State(state);
        Optional<Byte>[] scoreUpdates = move.apply(newState.board);
        for (Optional<Byte> maybePushedOff : scoreUpdates) {
            maybePushedOff.ifPresent(piece -> {
                if (piece == newState.maximizingPlayer)
                    newState.minPlayerScore += 1;
                else if (piece == newState.minimizingPlayer)
                    newState.maxPlayerScore += 1;
            });
        }

        if (movingPlayer == Board.WHITE)
            newState.movesLeftW -= 1;
        else if (movingPlayer == Board.BLACK)
            newState.movesLeftB -= 1;

        return newState;
    }
}