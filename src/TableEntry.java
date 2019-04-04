public class TableEntry {
    private int heuristicVal;
    private int depth;
    private ScoreType type;
    public enum ScoreType {
        UPPER_BOUND,
        LOWER_BOUND,
        EXACT_SCORE
    }
    public TableEntry(int heuristicVal, int alpha, int beta, int depth) {
        this.heuristicVal = heuristicVal;
        this.depth = depth;

        if (alpha<heuristicVal && heuristicVal<beta) {
            type = ScoreType.EXACT_SCORE;
        } else if (heuristicVal<=alpha) {
            type = ScoreType.UPPER_BOUND;
        } else if (heuristicVal>= beta) {
            type = ScoreType.LOWER_BOUND;
        }
    }

    public int fetchHeuristic() { return this.heuristicVal; }
    public int getDepth() { return this.depth; }
    public ScoreType getScoreType() { return this.type; }
}
