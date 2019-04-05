import java.util.List;

public class PrimaryHeuristic extends Heuristic {

    private static final int DISTANCE_TO_CENTER_WEIGHT = 25;
    private static final int SCORE_WEIGHT = 1000;
    private static final int LOSS_WEIGHT = 900;
    private static final int GROUPING_WEIGHT = 15;
    private static final int FORMATION_BREAK_WEIGHT = 30;
    private static final int MAX_AGGRESSION_WEIGHT = 100;
    private static final int MIN_AGGRESSION_WEIGHT = 50;

    @Override
    public int evaluate(final Minimax.State state) {
        if (Minimax.gameOver(state))
            return winLoss(state);

        List<OrderedMove> maxMoves = MoveGenerator.generate(state.board, state.maximizingPlayer, state.minimizingPlayer);
        List<OrderedMove> minMoves = MoveGenerator.generate(state.board, state.minimizingPlayer, state.maximizingPlayer);

        return closenessToCenter(state.board, state.maximizingPlayer) * DISTANCE_TO_CENTER_WEIGHT
                // Note that the score heuristic is not symmetrical.
                // Score-wise losses are only bad if they lead to a game loss.
                // However, they should still be accounted for as less marbles means a weaker position.
                + state.maxPlayerScore * SCORE_WEIGHT - state.minPlayerScore * LOSS_WEIGHT
                + (grouping(state.board, state.maximizingPlayer)
                - grouping(state.board, state.minimizingPlayer)) * GROUPING_WEIGHT
                + (formationBreak(state.board, state.maximizingPlayer, state.minimizingPlayer)
                - formationBreak(state.board, state.minimizingPlayer, state.maximizingPlayer)) * FORMATION_BREAK_WEIGHT
                + aggressionFactor(maxMoves) * MAX_AGGRESSION_WEIGHT
                - aggressionFactor(minMoves) * MIN_AGGRESSION_WEIGHT;
    }
}
