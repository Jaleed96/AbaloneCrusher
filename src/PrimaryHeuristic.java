import java.util.List;

public class PrimaryHeuristic extends Heuristic {

    private static final int SCORE_WEIGHT = 1000;

    @Override
    public int evaluate(final Minimax.State state) {
        if (Minimax.gameOver(state))
            return winLoss(state);

        List<OrderedMove> minMoves = MoveGenerator.generate(state.board, state.minimizingPlayer, state.maximizingPlayer);
        return closenessToCenter(state.board, state.maximizingPlayer)
                - aggressionFactor(minMoves)
                + state.maxPlayerScore * SCORE_WEIGHT - state.minPlayerScore * SCORE_WEIGHT;
    }
}
