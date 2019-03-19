public class Move {
    private final Push[] pushes;

    public Move(Push... pushes) {
        this.pushes = pushes;
    }

    public static class IllegalMoveException extends Exception {
        public IllegalMoveException(String msg) {
            super(msg);
        }
    }

    public Push[] pushes() {
        return pushes;
    }

    private static boolean isLegalOneStep(Board board, Push m) {
        byte[][] repr = board.representation();
        return repr[m.to.coordinate.y][m.to.coordinate.x] == Board.EMPTY && repr[m.from.y][m.from.x] == board.currentPlayer();
    }

    public static boolean isLegalInline(Board board, Push m) {
        byte[][] repr = board.representation();
        if (repr[m.from.y][m.from.x] != board.currentPlayer()) {
            return false;
        }
        int playerMarbleCnt = 1;
        int opponentMarbleCnt = 0;

        BoardUtil.Neighbor next = m.to;
        while (repr[next.coordinate.y][next.coordinate.x] == board.currentPlayer()) {
            playerMarbleCnt += 1;
            next = next.neighbors().fromDirection(m.to.direction);
            if (playerMarbleCnt == 4 || next == null)
                return false;
        }

        while (repr[next.coordinate.y][next.coordinate.x] == board.currentOpponent()) {
            opponentMarbleCnt += 1;
            next = next.neighbors().fromDirection(m.to.direction);
            if (next == null)
                return playerMarbleCnt > opponentMarbleCnt;
            if (playerMarbleCnt <= opponentMarbleCnt)
                return false;
        }

        return repr[next.coordinate.y][next.coordinate.x] == Board.EMPTY;
    }

    public boolean isLegalInline(Board board) {
        if (pushes.length != 1)
            return false;
        return isLegalInline(board, pushes[0]);
    }

    /// The in-between move cannot be the first or the last element of the array
    public boolean isLegalSideStep(Board board) {
        if (pushes.length == 0 || pushes.length > 3)
            // logic error
            return false;

        for (Push m : pushes) {
            if (!isLegalOneStep(board, m)) // each individual move is legal
                return false;
        }

        BoardUtil.Direction stepDirection = pushes[0].to.direction;
        for (int i = 1; i < pushes.length; ++i) {
            if (!BoardUtil.areNeighbors(pushes[i - 1].from, pushes[i].from)) // all from coordinates are neighbors
                return false;
            if (pushes[i].to.direction != stepDirection) // all are moving in the same direction
                return false;
        }

        // all are on the same diagonal
        return pushes.length != 3 || BoardUtil.findNeighborDirection(pushes[0].from, pushes[1].from) == BoardUtil.findNeighborDirection(pushes[1].from, pushes[2].from);
    }
}