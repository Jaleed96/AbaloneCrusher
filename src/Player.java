public class Player {
    public final Config.PlayerAgent agent;
    public final byte piece;
    private int score;
    private final int timeLimitMs;
    private final int moveLimit;
    private final Heuristic heuristic;

    Player(Config.PlayerAgent agent, byte piece, int moveLimit, int timeLimitMs, Heuristic heuristic) {
        this.agent = agent;
        this.piece = piece;
        this.score = 0;
        this.timeLimitMs = timeLimitMs;
        this.moveLimit = moveLimit;
        this.heuristic = heuristic;
    }

    Player(Player toCopy) {
        this.agent = toCopy.agent;
        this.piece = toCopy.piece;
        this.score = toCopy.score;
        this.timeLimitMs = toCopy.timeLimitMs;
        this.moveLimit = toCopy.moveLimit;
        this.heuristic = toCopy.heuristic;
    }

    public Heuristic heuristic() { return heuristic; }
    public int score() { return score; }
    public int increaseScore() { return ++score; }
    public int getTimeLimitMs() { return timeLimitMs; }
    public int getMoveLimit() { return moveLimit; }
}
