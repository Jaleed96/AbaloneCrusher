public class Player {
    public final byte piece;
    private int score;
    private final int timeLimitMs;
    private final int moveLimit;

    Player(byte piece, int moveLimit, int timeLimitMs) {
        this.piece = piece;
        this.score = 0;
        this.timeLimitMs = timeLimitMs;
        this.moveLimit = moveLimit;
    }

    Player(Player toCopy) {
        this.piece = toCopy.piece;
        this.score = toCopy.score;
        this.timeLimitMs = toCopy.timeLimitMs;
        this.moveLimit = toCopy.moveLimit;
    }

    public int score() { return score; }
    public int increaseScore() { return ++score; }
    public int getTimeLimitMs() { return timeLimitMs; }
    public int getMoveLimit() { return moveLimit; }
}
