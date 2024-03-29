public class Player {
    public final Config.PlayerAgent agent;
    public final byte piece;
    private int score;
    private final int timeLimitMs;
    private final int moveLimit;
    private final Heuristic heuristic;
    private final boolean usesIterativeDeepening;
    /** For depth limited search only */
    private final int depthLimit;

    Player(Config.PlayerAgent agent, byte piece, int moveLimit, int timeLimitMs, Heuristic heuristic, boolean usesIterativeDeepening, int depthLimit) {
        this.agent = agent;
        this.piece = piece;
        this.score = 0;
        this.timeLimitMs = timeLimitMs;
        this.moveLimit = moveLimit;
        this.heuristic = heuristic;
        this.usesIterativeDeepening = usesIterativeDeepening;
        this.depthLimit = depthLimit;
    }

    Player(Player toCopy) {
        this.agent = toCopy.agent;
        this.piece = toCopy.piece;
        this.score = toCopy.score;
        this.timeLimitMs = toCopy.timeLimitMs;
        this.moveLimit = toCopy.moveLimit;
        this.heuristic = toCopy.heuristic;
        this.usesIterativeDeepening = toCopy.usesIterativeDeepening;
        this.depthLimit = toCopy.depthLimit;
    }

    public Heuristic heuristic() { return heuristic; }
    public int score() { return score; }
    public int increaseScore() { return ++score; }
    public int getTimeLimitMs() { return timeLimitMs; }
    public int getMoveLimit() { return moveLimit; }
    public boolean usesIterativeDeepening() { return usesIterativeDeepening; }
    public int depthLimit() { return depthLimit; }
}
