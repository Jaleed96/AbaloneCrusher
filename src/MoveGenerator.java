import java.util.*;

// Generates legal moves for a given game state
public class MoveGenerator {

    // Assesses if a side step in the given direction is legal
    private static Optional<OrderedMove> maybeSideStep(byte[][] board, BoardUtil.Direction side, Coordinate... coords) {
        Push[] pushes = new Push[coords.length];
        for (int i = 0; i < coords.length; ++i) {
            // Check if all the marbles can be moved in the same direction
            BoardUtil.Neighbor neighbor = BoardUtil.neighborsOf(coords[i]).fromDirection(side);
            if (neighbor == null || board[neighbor.coordinate.y][neighbor.coordinate.x] != Board.EMPTY)
                return Optional.empty();
            pushes[i] = new Push(coords[i], neighbor);
        }
        return Optional.of(new OrderedMove(
                new Move(pushes),
                OrderedMove.sideStepType(pushes.length)
        ));
    }

    // Finds all legal sidesteps in the forward left/right directions
    private static List<OrderedMove> legalForwardSideSteps(byte[][] board, BoardUtil.Direction dir, Coordinate... coords) {
        List<OrderedMove> moves = new ArrayList<>();
        maybeSideStep(board, dir.forwardLeft(), coords).ifPresent(moves::add);
        maybeSideStep(board, dir.forwardRight(), coords).ifPresent(moves::add);
        return moves;
    }

    // Collects all the legal moves from a coordinate in a given direction
    private static List<OrderedMove> collectFromDirection(byte[][] board, byte playerPiece, byte opponentPiece, Coordinate from, BoardUtil.Direction dir) {
        List<OrderedMove> moves = new ArrayList<>();

        int playerMarbleCnt = 1;
        int opponentMarbleCnt = 0;

        Coordinate middle = null;
        // Save to destination for making inline moves
        BoardUtil.Neighbor to = BoardUtil.neighborsOf(from).fromDirection(dir);
        BoardUtil.Neighbor next = to;
        // Count player's pieces
        while (board[next.coordinate.y][next.coordinate.x] == playerPiece) {
            playerMarbleCnt += 1;
            // At most this will add 4 side steps, because we only inspect 2-marble and 3-marble lines starting from the "from" coordinate
            // and we only consider the forward left/right directions.
            // This is enough because every player's marble on the board and every direction gets the same treatment.
            if (playerMarbleCnt == 2) {
                middle = next.coordinate;
                // potential 2-marble side steps
                moves.addAll(legalForwardSideSteps(board, dir, from, middle));
            } else if (playerMarbleCnt == 3) {
                // potential 3-marble side steps
                moves.addAll(legalForwardSideSteps(board, dir, from, middle, next.coordinate));
            }
            next = next.neighbors().fromDirection(dir);
            // illegal inline move; next==null (off of board) may be changed to legal if we want to self-eliminate
            if (next == null || playerMarbleCnt == 4)
                return moves;
        }

        boolean canPush;
        // Count opponents pieces. At this point we are deciding if (from, to) is a legal inline move.
        while (board[next.coordinate.y][next.coordinate.x] == opponentPiece) {
            opponentMarbleCnt += 1;
            next = next.neighbors().fromDirection(dir);
            canPush = playerMarbleCnt > opponentMarbleCnt;
            if (next == null || !canPush) {
                if (canPush) {
                    // Pushing opponent's marbles
                    moves.add(new OrderedMove(
                            new Move(new Push(from, to)),
                            OrderedMove.inlineType(playerMarbleCnt, opponentMarbleCnt, true)
                    ));
                }
                return moves;
            }
        }

        // Inline move ending with an empty cell
        if (board[next.coordinate.y][next.coordinate.x] == Board.EMPTY) {
            moves.add(new OrderedMove(
                    new Move(new Push(from, to)),
                    OrderedMove.inlineType(playerMarbleCnt, opponentMarbleCnt, false)
            ));
        }

        return moves;
    }

    // Collects all the legal moves starting at a given coordinate
    private static List<OrderedMove> collectFromCoord(byte[][] board, byte playerPiece, byte opponentPiece, Coordinate from) {
        List<OrderedMove> moves = new ArrayList<>();
        BoardUtil.Neighbors neighbors = BoardUtil.neighborsOf(from);
        // to allow pushing own pieces off the board, make sure neighbors array includes null neighbors
        // add additional null checks to collectFromDirection
        for (BoardUtil.Neighbor to : neighbors.toArray())
            moves.addAll(collectFromDirection(board, playerPiece, opponentPiece, from, to.direction));
        return moves;
    }

    public static List<OrderedMove> generate(byte[][] board, byte playerPiece, byte opponentPiece) {
        List<OrderedMove> moves = new ArrayList<>();
        for (Coordinate[] row : BoardUtil.COORDINATES) {
            for (Coordinate coord : row)
                // All the private functions assume that the given coordinates contain the current player's piece
                if (board[coord.y][coord.x] == playerPiece)
                    moves.addAll(collectFromCoord(board, playerPiece, opponentPiece, coord));
        }
        return moves;
    }
    //gets a random move generated from
    public static Move firstRandMove(byte[][] board) {
        List<OrderedMove> moves = generate(board, Board.BLACK, Board.WHITE);
        moves.sort(Comparator.comparing(orderedMove -> orderedMove.type));
        Random r = new Random();
        //gets top 10 first random moves
        int randInd =  r.nextInt(11);
        OrderedMove move = moves.get(randInd);
        return move.move;
    }
}
