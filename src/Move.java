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

    private static boolean isLegalOneStep(byte[][] board, byte playerPiece, Push m) {
        return board[m.to.coordinate.y][m.to.coordinate.x] == Board.EMPTY && board[m.from.y][m.from.x] == playerPiece;
    }

    public static boolean isLegalInline(byte[][] board, byte playerPiece, byte opponentPiece, Push m) {
        if (board[m.from.y][m.from.x] != playerPiece) {
            return false;
        }

        int playerMarbleCnt = 1;
        int opponentMarbleCnt = 0;

        BoardUtil.Neighbor next = m.to;
        while (board[next.coordinate.y][next.coordinate.x] == playerPiece) {
            playerMarbleCnt += 1;
            next = next.neighbors().fromDirection(m.to.direction);
            if (playerMarbleCnt == 4 || next == null)
                return false;
        }

        while (board[next.coordinate.y][next.coordinate.x] == opponentPiece) {
            opponentMarbleCnt += 1;
            next = next.neighbors().fromDirection(m.to.direction);
            if (next == null)
                return playerMarbleCnt > opponentMarbleCnt;
            if (playerMarbleCnt <= opponentMarbleCnt)
                return false;
        }

        return board[next.coordinate.y][next.coordinate.x] == Board.EMPTY;
    }

    public boolean hasValidOneSteps(byte[][] board, byte playerPiece) {
        for (Push m : pushes) {
            if (!isLegalOneStep(board, playerPiece, m)) // each individual move is legal
                return false;
        }
        return true;
    }

    public boolean isLegalInline(byte[][] board, byte playerPiece, byte opponentPiece) {
        if (pushes.length != 1)
            return false;
        return isLegalInline(board, playerPiece, opponentPiece, pushes[0]);
    }

    public boolean isLegalSideStep(byte[][] board, byte playerPiece) {
        if (pushes.length == 0 || pushes.length > 3)
            // logic error
            return false;

        if (!hasValidOneSteps(board, playerPiece))
            return false;

        BoardUtil.Direction stepDirection = pushes[0].to.direction;
        for (int i = 1; i < pushes.length; ++i) {
            if (pushes[i].to.direction != stepDirection) // all are moving in the same direction
                return false;
        }

        if (pushes.length == 3)
            return BoardUtil.onSameAxis(pushes[0].from, pushes[1].from, pushes[2].from)
                && BoardUtil.areNeighbors(pushes[0].from, pushes[1].from, pushes[2].from);

        return pushes.length == 1 || BoardUtil.areNeighbors(pushes[0].from, pushes[1].from);
    }

    public boolean isLegal(Board context) {
        return isLegalInline(context.representation(), context.currentPlayer().piece, context.currentOpponent().piece)
                || isLegalSideStep(context.representation(), context.currentPlayer().piece);
    }
}
