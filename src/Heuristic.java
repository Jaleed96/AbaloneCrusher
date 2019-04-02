public class Heuristic {
    public static final int[][] CACHED_CENTER_DIST_SCORE = cacheCenterDist();

    private static int[][] cacheCenterDist() {
        int maxDistFromCenter = 4;
        int[][] dist = new int[BoardUtil.STANDARD_LAYOUT.length][];
        for (int row = 0; row < dist.length; ++row) {
            dist[row] = new int[BoardUtil.STANDARD_LAYOUT[row].length];
            for (int col = 0; col < dist[row].length; ++col)
                dist[row][col] = maxDistFromCenter - BoardUtil.manhattanDistance(BoardUtil.COORDINATES[row][col], BoardUtil.COORDINATES[4][4]);
        }
        return dist;
    }

    private static final int DISTANCE_TO_CENTER_WEIGHT = 10;

    /** Reverse Manhattan distance of all player's pieces to the center of the board,
     * i.e. at center dist = 4, 4 away from center = 0 */
    private static int closenessToCenter(byte[][] board, byte player) {
        int totalScore = 0;
        for (int row = 0; row < board.length; ++row) {
            for (int col = 0; col < board[row].length; ++col) {
                if (board[row][col] == player)
                    totalScore += CACHED_CENTER_DIST_SCORE[row][col];
            }
        }
        return totalScore;
    }

    private static int winLoss(Minimax.State state) {
        if (state.maxPlayerScore == Board.SCORE_TO_WIN)
            return Integer.MAX_VALUE;
        if (state.minPlayerScore == Board.SCORE_TO_WIN)
            return Integer.MIN_VALUE;
        if (state.movesLeftB == 0 && state.movesLeftW == 0) {
            if (state.maxPlayerScore > state.minPlayerScore)
                return Integer.MAX_VALUE;
            if (state.maxPlayerScore < state.minPlayerScore)
                return Integer.MIN_VALUE;
            // what to do in case of draw?
        }

        return 0;
    }

    public static int evaluate(Minimax.State state) {
        int winLossVal = winLoss(state);
        if (winLossVal != 0)
            return winLossVal;

        return closenessToCenter(state.board, state.maximizingPlayer) * DISTANCE_TO_CENTER_WEIGHT;
    }
}
