public class ExperimentalHeuristic extends Heuristic {

    private static final int SCORE_WEIGHT = 1000;

    @Override
    public int evaluate(final Minimax.State state) {
        if (Minimax.gameOver(state))
            return winLoss(state);

        return closenessToCenter(state.board, state.maximizingPlayer)
                + state.maxPlayerScore * SCORE_WEIGHT - state.minPlayerScore * SCORE_WEIGHT;
    }
}
