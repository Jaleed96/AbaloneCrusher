import java.util.List;

/** Minimax algorithm with alpha-beta pruning */
public class Minimax {

    /** Smaller version of Gamestate class, there's some code duplication here */
    static class State {
        final byte[][] board;
        final byte player;
        final byte opponent;
        int movesLeftB;
        int movesLeftW;
        int scoreB;
        int scoreW;

        State(byte[][] board, byte player, byte opponent, int movesLeftB, int movesLeftW, int scoreB, int scoreW) {
            // TODO reverse move instead of copy every time
            this.board = BoardUtil.deepCopyRepresentation(board);
            this.player = player;
            this.opponent = opponent;
            this.movesLeftB = movesLeftB;
            this.movesLeftW = movesLeftW;
            this.scoreB = scoreB;
            this.scoreW = scoreW;
        }
    }

    public static Move searchBestMove(State state) {
        List<Move> moves = MoveGenerator.generate(state.board, state.player, state.opponent);
        Move bestMove = moves.get(0);
        int bestScore = Integer.MIN_VALUE;
        for (Move m : moves) {
            // passing bestScore to make sure that alpha propagates to next level
            int score = maximize(state, bestScore, Integer.MAX_VALUE, 3);
            if (score > bestScore) {
                bestScore = score;
                bestMove = m;
            }
        }
        return bestMove;
    }

    /** toState is the move that was taken to reach the given state */
    private static int maximize(State state, int alpha, int beta, int depth) {
        if (gameOver(state) || depth == 0)
            return Heuristic.evaluate(state);

        int val = Integer.MIN_VALUE;
        List<Move> moves = MoveGenerator.generate(state.board, state.player, state.opponent);
        for (Move m : moves) {
            val = Math.max(val, minimize(moveResult(state, m), alpha, beta, depth - 1));
            if (val >= beta) return val;
            alpha = Math.max(alpha, val);
        }

        return val;
    }

    private static int minimize(State state, int alpha, int beta, int depth) {
        if (gameOver(state) || depth == 0)
            return Heuristic.evaluate(state);

        int val = Integer.MAX_VALUE;
        List<Move> moves = MoveGenerator.generate(state.board, state.player, state.opponent);
        for (Move m : moves) {
            val = Math.min(val, maximize(moveResult(state, m), alpha, beta, depth - 1));
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

    private static State moveResult(State state, Move move) {
        final State newState = new State(state.board, state.opponent, state.player, state.movesLeftB, state.movesLeftW, state.scoreB, state.scoreW);

        move.apply(newState.board).ifPresent(piece -> {
            if (piece == Board.WHITE)
                newState.scoreB += 1;
            else if (piece == Board.BLACK)
                newState.scoreW += 1;
        });

        if (state.player == Board.WHITE)
            newState.movesLeftW -= 1;
        else if (state.player == Board.BLACK)
            newState.movesLeftB -= 1;

        return newState;
    }
}