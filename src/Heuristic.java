import java.util.List;
import java.util.Optional;

public abstract class Heuristic {
    private static final int[][] CACHED_CENTER_DIST_SCORE = cacheCenterDist();

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

    static int logScale(int val) {
        if (val <= 0) return 0;
        return (int) Math.ceil(Math.log(val));
    }

    static int quadraticScale(int val) {
        return val * val;
    }

    /** Reverse Manhattan distance of all player's pieces to the center of the board,
     * i.e. at center dist = 4, 4 away from center = 0 */
    static int closenessToCenter(byte[][] board, byte player) {
        int totalScore = 0;
        for (int row = 0; row < board.length; ++row) {
            for (int col = 0; col < board[row].length; ++col) {
                if (board[row][col] == player)
                    totalScore += CACHED_CENTER_DIST_SCORE[row][col];
            }
        }
        return totalScore;
    }

    static int distanceFromCenter(byte[][] board, byte player) {
        int maxDistFromCenter = 4;
        int totalScore = 0;
        for (int row = 0; row < board.length; ++row) {
            for (int col = 0; col < board[row].length; ++col) {
                if (board[row][col] == player)
                    totalScore += maxDistFromCenter - CACHED_CENTER_DIST_SCORE[row][col];
            }
        }
        return totalScore;
    }

    static int piecesOnEdge(byte[][] board, byte player) {
        int piecesOnEdge = 0;
        for (byte b : board[0])
            piecesOnEdge += b == player ? 1 : 0;

        for (byte b : board[board.length - 1])
            piecesOnEdge += b == player ? 1 : 0;

        for (int row = 1; row < board.length - 1; ++row) {
            piecesOnEdge += board[row][0] == player ? 1 : 0;
            piecesOnEdge += board[row][board[row].length - 1] == player ? 1 : 0;
        }

        return piecesOnEdge;
    }

    /** Finds how many friendly neighbors each of the player's pieces has */
    static int grouping(byte[][] board, byte player) {
        int grouping = 0;
        for (int row = 0; row < board.length; ++row) {
            for (int col = 0; col < board[row].length; ++col) {
                if (board[row][col] == player) {
                    BoardUtil.Neighbors neighbors = BoardUtil.neighborsOf(BoardUtil.COORDINATES[row][col]);
                    for (BoardUtil.Neighbor n : neighbors.toArray()) {
                        if (board[n.coordinate.y][n.coordinate.x] == player)
                            grouping += 1;
                    }
                }
            }
        }
        return grouping;
    }

    private static int form3InDir(byte[][] board, byte player, Coordinate c, BoardUtil.Direction dir) {
        BoardUtil.Neighbor neighbor = BoardUtil.neighborsOf(c).fromDirection(dir);
        if (neighbor != null && board[neighbor.coordinate.y][neighbor.coordinate.x] == player) {
            BoardUtil.Neighbor next = neighbor.neighbors().fromDirection(dir);
            if (next != null && board[next.coordinate.y][next.coordinate.x] == player)
                return 1;
        }

        return 0;
    }

    /** Count the number of 3 inline piece formations a player has */
    static int formationsOfThree(byte[][] board, byte player) {
        int formations = 0;
        for (int row = 0; row < board.length; ++row) {
            for (int col = 0; col < board[row].length; ++col) {
                if (board[row][col] == player) {
                    formations += form3InDir(board, player, BoardUtil.COORDINATES[row][col], BoardUtil.Direction.E)
                                + form3InDir(board, player, BoardUtil.COORDINATES[row][col], BoardUtil.Direction.NE)
                                + form3InDir(board, player, BoardUtil.COORDINATES[row][col], BoardUtil.Direction.NW);
                }
            }
        }
        return formations;
    }

    /** Formation break happens when a player's marble is between two opponents marbles.
     * @return the number of "broken up" opponent's marbles, i.e. WBW has value of 2 for B player*/
    static int formationBreak(byte[][] board, byte player, byte opponent) {
        int formationBreak = 0;
        for (int row = 0; row < board.length; ++row) {
            for (int col = 0; col < board[row].length; ++col) {
                if (board[row][col] == player) {
                    BoardUtil.Neighbors neighbors = BoardUtil.neighborsOf(BoardUtil.COORDINATES[row][col]);
                    for (BoardUtil.Neighbor n : neighbors.toArray()) {
                        if (board[n.coordinate.y][n.coordinate.x] == opponent) {
                            formationBreak += Optional.ofNullable(neighbors.fromDirection(n.direction.opposite()))
                                                      .map(neighbor -> board[neighbor.coordinate.y][neighbor.coordinate.x] == opponent ? 1 : null)
                                                      .orElse(0);
                        }
                    }
                }
            }
        }
        return formationBreak;
    }

    static int aggressionFactor(List<OrderedMove> possibleMoves) {
        final int capture = 2;
        final int push = 1;

        int aggression = 0;
        for (OrderedMove om : possibleMoves) {
            switch (om.type) {
                case THREE_PUSH_TWO_CAPTURE: case THREE_PUSH_ONE_CAPTURE: case TWO_PUSH_ONE_CAPTURE:
                    aggression += capture;
                    break;
                case THREE_PUSH_TWO: case THREE_PUSH_ONE: case TWO_PUSH_ONE:
                    aggression += push;
                    break;
                default: break;
            }
        }
        return aggression;
    }

    static int winLoss(Minimax.State state) {
        if (state.maxPlayerScore == Board.SCORE_TO_WIN)
            return Integer.MAX_VALUE;
        if (state.minPlayerScore == Board.SCORE_TO_WIN)
            return Integer.MIN_VALUE;
        if (state.movesLeftB == 0 && state.movesLeftW == 0) {
            if (state.maxPlayerScore > state.minPlayerScore)
                return Integer.MAX_VALUE;
            if (state.maxPlayerScore < state.minPlayerScore)
                return Integer.MIN_VALUE;
            return Integer.MIN_VALUE / 2;
        }

        return 0;
    }

    public abstract int evaluate(final Minimax.State state);
}
