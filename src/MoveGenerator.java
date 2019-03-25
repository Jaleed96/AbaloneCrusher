import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

// Generates legal moves for a given game state
public class MoveGenerator {

    // Assesses if a side step in the given direction is legal
    private static Optional<Move> maybeSideStep(byte[][] board, BoardUtil.Direction side, Coordinate... coords) {
        Push[] pushes = new Push[coords.length];
        for (int i = 0; i < coords.length; ++i) {
            BoardUtil.Neighbor neighbor = BoardUtil.neighborsOf(coords[i]).fromDirection(side);
            if (neighbor == null || board[neighbor.coordinate.y][neighbor.coordinate.x] != Board.EMPTY)
                return Optional.empty();
            pushes[i] = new Push(coords[i], neighbor);
        }
        return Optional.of(new Move(pushes));
    }

    // Finds all legal sidesteps for coordinates in the forward direction (only 2 possible)
    private static List<Move> legalForwardSideSteps(byte[][] board, BoardUtil.Direction dir, Coordinate... coords) {
        List<Move> moves = new ArrayList<>();
        maybeSideStep(board, dir.forwardLeft(), coords).ifPresent(moves::add);
        maybeSideStep(board, dir.forwardRight(), coords).ifPresent(moves::add);
        return moves;
    }

    // Collects all the legal moves from a coordinate in a given direction
    private static List<Move> collectFromDirection(byte[][] board, byte playerPiece, byte opponentPiece, Coordinate from, BoardUtil.Direction dir) {
        List<Move> moves = new ArrayList<>();

        int playerMarbleCnt = 1;
        int opponentMarbleCnt = 0;

        Coordinate middle = null;
        // Save to destination for making inline moves
        BoardUtil.Neighbor to = BoardUtil.neighborsOf(from).fromDirection(dir);
        BoardUtil.Neighbor next = to;
        while (board[next.coordinate.y][next.coordinate.x] == playerPiece) {
            playerMarbleCnt += 1;
            if (playerMarbleCnt == 2) {
                middle = next.coordinate;
                moves.addAll(legalForwardSideSteps(board, dir, from, middle));
            } else if (playerMarbleCnt == 3) {
                moves.addAll(legalForwardSideSteps(board, dir, from, middle, next.coordinate));
            }
            next = next.neighbors().fromDirection(dir);
            // illegal inline move
            if (next == null || playerMarbleCnt == 4)
                return moves;
        }

        boolean canPush;
        while (board[next.coordinate.y][next.coordinate.x] == opponentPiece) {
            opponentMarbleCnt += 1;
            next = next.neighbors().fromDirection(dir);
            canPush = playerMarbleCnt > opponentMarbleCnt;
            if (next == null || !canPush) {
                if (canPush)
                    moves.add(new Move(new Push(from, to)));
                return moves;
            }
        }

        if (board[next.coordinate.y][next.coordinate.x] == Board.EMPTY)
            moves.add(new Move(new Push(from, to)));

        return moves;
    }

    // Collects all the legal moves starting at a given coordinate
    private static List<Move> collectFromCoord(byte[][] board, byte playerPiece, byte opponentPiece, Coordinate from) {
        List<Move> moves = new ArrayList<>();
        BoardUtil.Neighbors neighbors = BoardUtil.neighborsOf(from);
        // to allow pushing own pieces off the board, make sure neighbors array includes null neighbors
        // add additional null checks to collectFromDirection
        for (BoardUtil.Neighbor to : neighbors.toArray())
            moves.addAll(collectFromDirection(board, playerPiece, opponentPiece, from, to.direction));
        return moves;
    }

    public static List<Move> generate(byte[][] board, byte playerPiece, byte opponentPiece) {
        List<Move> moves = new ArrayList<>();
        for (Coordinate[] row : BoardUtil.COORDINATES) {
            for (Coordinate coord : row)
                // All the private functions assume that the given coordinates contain the current player's piece
                if (board[coord.y][coord.x] == playerPiece)
                    moves.addAll(collectFromCoord(board, playerPiece, opponentPiece, coord));
        }
        return moves;
    }
}
