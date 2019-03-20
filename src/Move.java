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

    private static boolean isLegalOneStep(Board context, Push m) {
        byte[][] repr = context.representation();
        return repr[m.to.coordinate.y][m.to.coordinate.x] == Board.EMPTY && repr[m.from.y][m.from.x] == context.currentPlayer().piece;
    }

    public static boolean isLegalInline(Board context, Push m) {
        byte[][] repr = context.representation();
        if (repr[m.from.y][m.from.x] != context.currentPlayer().piece) {
            return false;
        }
        int playerMarbleCnt = 1;
        int opponentMarbleCnt = 0;

        BoardUtil.Neighbor next = m.to;
        while (repr[next.coordinate.y][next.coordinate.x] == context.currentPlayer().piece) {
            playerMarbleCnt += 1;
            next = next.neighbors().fromDirection(m.to.direction);
            if (playerMarbleCnt == 4 || next == null)
                return false;
        }

        while (repr[next.coordinate.y][next.coordinate.x] == context.currentOpponent().piece) {
            opponentMarbleCnt += 1;
            next = next.neighbors().fromDirection(m.to.direction);
            if (next == null)
                return playerMarbleCnt > opponentMarbleCnt;
            if (playerMarbleCnt <= opponentMarbleCnt)
                return false;
        }

        return repr[next.coordinate.y][next.coordinate.x] == Board.EMPTY;
    }

    public boolean isLegalInline(Board context) {
        if (pushes.length != 1)
            return false;
        return isLegalInline(context, pushes[0]);
    }

    public boolean isLegalSideStep(Board context) {
        if (pushes.length == 0 || pushes.length > 3)
            // logic error
            return false;

        for (Push m : pushes) {
            if (!isLegalOneStep(context, m)) // each individual move is legal
                return false;
        }

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
        return isLegalInline(context) || isLegalSideStep(context);
    }
}
