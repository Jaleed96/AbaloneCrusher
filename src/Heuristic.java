public class Heuristic {
    public static final int[][] CACHED_DIST = cacheCenterDist();

    private static int[][] cacheCenterDist() {
        int[][] dist = new int[BoardUtil.STANDARD_LAYOUT.length][];
        for (int row = 0; row < dist.length; ++row) {
            dist[row] = new int[BoardUtil.STANDARD_LAYOUT[row].length];
            for (int col = 0; col < dist[row].length; ++col)
                dist[row][col] = BoardUtil.manhattanDistance(BoardUtil.COORDINATES[row][col], BoardUtil.COORDINATES[4][4]);
        }
        return dist;
    }

    private static final int DISTANCE_TO_CENTER_WEIGHT = 10;

    /** Manhattan distance of all player's pieces to the center of the board */
    private static int closenessToCenter(Minimax.State state) {
        int totalDist = 14 * 4; // 14 marbles * 4 maximum manhattan distance from center
        for (int row = 0; row < state.board.length; ++row) {
            for (int col = 0; col < state.board[row].length; ++col) {
                if (state.board[row][col] == state.player)
                    totalDist -= CACHED_DIST[row][col];
            }
        }
        return totalDist;
    }

    public static int evaluate(Minimax.State state) {
        return closenessToCenter(state) * DISTANCE_TO_CENTER_WEIGHT;
    }
}
