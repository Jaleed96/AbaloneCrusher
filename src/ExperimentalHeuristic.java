import java.util.List;

public class ExperimentalHeuristic extends Heuristic {

    private static final int SCORE_WEIGHT = 1000;

    @Override
    public int evaluate(final Minimax.State state) {
        if (Minimax.gameOver(state))
            return winLoss(state);
        //geometricCentroidScore(state, 0.6, 0.8, 0.9)

//        if (state.movesLeftB > 30) {
//            return closenessToCenter(state.board, state.maximizingPlayer)
//                    + state.maxPlayerScore * SCORE_WEIGHT - state.minPlayerScore * SCORE_WEIGHT;
//        } else if (state.movesLeftB <= 30 && state.movesLeftB > 20 || state.maxPlayerScore>state.minPlayerScore) {
//            List<OrderedMove> minMoves = MoveGenerator.generate(state.board, state.minimizingPlayer, state.maximizingPlayer);
//            return closenessToCenter(state.board, state.maximizingPlayer)
//                    - aggressionFactor(minMoves)
//                    + state.maxPlayerScore * SCORE_WEIGHT - state.minPlayerScore * SCORE_WEIGHT;
//        }
//        List<OrderedMove> maxMoves = MoveGenerator.generate(state.board, state.maximizingPlayer, state.minimizingPlayer);
//        return closenessToCenter(state.board, state.maximizingPlayer)
//                + aggressionFactor(maxMoves)
//                + state.maxPlayerScore * SCORE_WEIGHT - state.minPlayerScore * SCORE_WEIGHT;
        List<OrderedMove> minMoves = MoveGenerator.generate(state.board, state.minimizingPlayer, state.maximizingPlayer);
        return closenessToCenter(state.board, state.maximizingPlayer) * 50
                + grouping(state.board, state.maximizingPlayer) * 20
                - aggressionFactor(minMoves) * 50
                + state.maxPlayerScore * SCORE_WEIGHT - state.minPlayerScore * SCORE_WEIGHT;
    }
}
