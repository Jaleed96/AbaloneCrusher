/** Moves that can be ordered based on how promising they are */
public class OrderedMove implements Comparable {

    @Override
    public int compareTo(Object o) {
        OrderedMove other = (OrderedMove) o;
        return this.type.compareTo(other.type);
    }

    /** The position in this enum defines move ordering */
    public enum MoveType {
        THREE_PUSH_TWO_CAPTURE,
        THREE_PUSH_ONE_CAPTURE,
        TWO_PUSH_ONE_CAPTURE,
        THREE_PUSH_TWO,
        THREE_PUSH_ONE,
        TWO_PUSH_ONE,
        THREE_INLINE,
        TWO_INLINE,
        THREE_SIDESTEP,
        TWO_SIDESTEP,
        ONE
    }

    static MoveType inlineType(int playerPieceCount, int opponentPieceCount, boolean capture) {
        if (playerPieceCount == 3) {
            if (opponentPieceCount == 2)
                return capture ? MoveType.THREE_PUSH_TWO_CAPTURE : MoveType.THREE_PUSH_TWO;
            else if (opponentPieceCount == 1)
                return capture ? MoveType.THREE_PUSH_ONE_CAPTURE : MoveType.THREE_PUSH_ONE;
            else /* opponentPieceCount == 0) */
                return MoveType.THREE_INLINE;
        } else if (playerPieceCount == 2) {
            if (opponentPieceCount == 1)
                return capture ? MoveType.TWO_PUSH_ONE_CAPTURE : MoveType.TWO_PUSH_ONE;
            else /* opponentPieceCount == 0) */
                return MoveType.TWO_INLINE;
        } else  /* playerPieceCount == 1 */
            return MoveType.ONE;
    }

    static MoveType sideStepType(int numPieces) {
        if (numPieces == 2)
            return MoveType.TWO_SIDESTEP;
        else /* numPieces == 3 */
            return MoveType.THREE_SIDESTEP;
    }

    final Move move;
    final MoveType type;

    OrderedMove(Move move, MoveType type) {
        this.move = move;
        this.type = type;
    }


}
