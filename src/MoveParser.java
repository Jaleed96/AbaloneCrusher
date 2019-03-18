// Example inline move "H5 to G5"
//         side move  "H5-H7 to G4" would move H5 to G4, H6 to G5, H7 to G6
//                    destination coordinate is always for the first marble in the specified range
public class MoveParser {

    private static final String FROM_TO_SEPARATOR = " TO ";
    private static final String RANGE_SEPARATOR = "-";

    /// Returns an array of parsed moves or throws a ParseException if failed to parse
    /// Does not check for legality of moves but ensures that source and destination coordinates are adjacent and not null
    public static Move parse(String input) throws Exception {
        input = input.trim().toUpperCase();

        String[] fromTo = input.split(FROM_TO_SEPARATOR);

        if (fromTo.length < 2)
            throw new Exception("Not enough arguments");

        Coordinate to = BoardUtil.toCoord(fromTo[1].trim());
        if (to == null)
            throw new Exception("Failed to parse destination coordinate");

        String[] maybeRange = fromTo[0].split(RANGE_SEPARATOR);
        if (maybeRange.length == 1) {
            Coordinate from = BoardUtil.toCoord(maybeRange[0].trim());
            if (from == null)
                throw new Exception("Failed to parse source coordinate");

            BoardUtil.Neighbor toNeighbor = BoardUtil.neighborsOf(from).fromCoordinate(to);
            if (toNeighbor == null)
                throw new Exception("Destination coordinate is not adjacent to source coordinate");

            return new Move(new Push(from, toNeighbor));
        } else if (maybeRange.length == 2) {
            Coordinate fromFirst = BoardUtil.toCoord(maybeRange[0].trim());
            if (fromFirst == null)
                throw new Exception("Failed to parse first range coordinate");

            Coordinate fromLast = BoardUtil.toCoord(maybeRange[1].trim());
            if (fromLast == null)
                throw new Exception("Failed to parse last range coordinate");

            BoardUtil.Neighbor toFirstNeighbor = BoardUtil.neighborsOf(fromFirst).fromCoordinate(to);
            if (toFirstNeighbor == null)
                throw new Exception("Destination coordinate is not adjacent to first range coordinate");

            BoardUtil.Direction moveDirection = toFirstNeighbor.direction;
            BoardUtil.Neighbor toLastNeighbor = BoardUtil.neighborsOf(fromLast).fromDirection(moveDirection);

            if (BoardUtil.areNeighbors(fromFirst, fromLast)) // then it's a 2-piece side move
                return new Move(new Push(fromFirst, toFirstNeighbor),
                                new Push(fromLast, toLastNeighbor));
            else {
                Coordinate between = BoardUtil.findCoordBetween(fromFirst, fromLast);
                if (between == null)
                    throw new Exception("Failed to find coordinate between first and last");

                BoardUtil.Neighbor betweenNeighbor = BoardUtil.neighborsOf(between).fromDirection(moveDirection);
                if (betweenNeighbor == null)
                    throw new Exception("Middle coordinate has nowhere to move"); // should never happen

                return new Move(new Push(fromFirst, toFirstNeighbor),
                                new Push(between, betweenNeighbor),
                                new Push(fromLast, toLastNeighbor));
            }
        }

        throw new Exception("Failed to parse input");
    }
}
