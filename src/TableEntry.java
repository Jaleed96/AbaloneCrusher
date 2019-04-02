public class TableEntry {
    private Move bestMove;
    private Minimax.State state;
    private int alpha;
    private int beta;
    private int depth;
    public TableEntry(Move m, Minimax.State state, int alpha, int beta, int depth) {
        this.bestMove = m;
        this.state = state;
        this.alpha = alpha;
        this.beta = beta;
        this.depth = depth;
    }

    public Move getBestMove() { return this.bestMove; }
    public Minimax.State getState() { return this.state; }
    public int getAlpha() { return this.alpha; }
    public int getBeta() { return this.beta; }
    public int getDepth() { return this.depth; }
}
