public class TableEntry {
    private int heuristicVal;
    private Minimax.State state;
    private int alpha;
    private int beta;
    private int depth;
    public TableEntry(int heuristicVal, Minimax.State state, int alpha, int beta, int depth) {
        this.heuristicVal = heuristicVal;
        this.state = state;
        this.alpha = alpha;
        this.beta = beta;
        this.depth = depth;
    }

    public int fetchHeuristic() { return this.heuristicVal; }
    public Minimax.State getState() { return this.state; }
    public int getAlpha() { return this.alpha; }
    public int getBeta() { return this.beta; }
    public int getDepth() { return this.depth; }
}
