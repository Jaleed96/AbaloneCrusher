import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class BoardUtil {

    /// Cardinal directions
    public enum Direction {
        NW, W, SW, SE, E, NE
    }

    public static class Neighbor {
        public final Coordinate coordinate;
        public final Direction direction;

        public static Neighbor makeNeighbor(Coordinate coordinate, Direction direction) {
            if (coordinate == null || direction == null)
                return null;
            return new Neighbor(coordinate, direction);
        }

        private Neighbor(Coordinate coordinate, Direction direction) {
            this.coordinate = coordinate;
            this.direction = direction;
        }

        public Neighbors neighbors() {
            return neighborsOf(coordinate);
        }
    }

    /// Maps neighboring cells to their coordinates and directions
    /// A neighbor is null if it's out of bounds
    public static class Neighbors {
        public final Neighbor NW, W, SW, SE, E, NE;
        private final Neighbor[] index;
        private final Neighbor[] nonNullNeighbors;

        Neighbors(Coordinate NW,
                  Coordinate W,
                  Coordinate SW,
                  Coordinate SE,
                  Coordinate E,
                  Coordinate NE)
        {
            this.NW = Neighbor.makeNeighbor(NW, Direction.NW);
            this.W = Neighbor.makeNeighbor(W, Direction.W);
            this.SW = Neighbor.makeNeighbor(SW, Direction.SW);
            this.SE = Neighbor.makeNeighbor(SE, Direction.SE);
            this.E = Neighbor.makeNeighbor(E, Direction.E);
            this.NE = Neighbor.makeNeighbor(NE, Direction.NE);
            index = new Neighbor[] {this.NW, this.W, this.SW, this.SE, this.E, this.NE};
            nonNullNeighbors = Arrays.stream(index).filter(Objects::nonNull).toArray(Neighbor[]::new);
        }

        public Neighbor[] toArray() {
            return nonNullNeighbors;
        }

        public Neighbor fromDirection(Direction dir) {
            return index[dir.ordinal()];
        }

        public Neighbor fromCoordinate(Coordinate coord) {
            for (Neighbor n : nonNullNeighbors) {
                if (n.coordinate.equals(coord))
                    return n;
            }
            return null;
        }
    }

    private static final byte E = Board.EMPTY, W = Board.WHITE, B = Board.BLACK;

    private static final byte[][] STANDARD_LAYOUT = {
                {W,W,W,W,W},
               {W,W,W,W,W,W},
              {E,E,W,W,W,E,E},
             {E,E,E,E,E,E,E,E},
            {E,E,E,E,E,E,E,E,E},
             {E,E,E,E,E,E,E,E},
              {E,E,B,B,B,E,E},
               {B,B,B,B,B,B},
                {B,B,B,B,B}
    };

    private static final byte[][] GERMAN_DAISY_LAYOUT = {
                {E,E,E,E,E},
               {W,W,E,E,B,B},
              {W,W,W,E,B,B,B},
             {E,W,W,E,E,B,B,E},
            {E,E,E,E,E,E,E,E,E},
             {E,B,B,E,E,W,W,E},
              {B,B,B,E,W,W,W},
               {B,B,E,E,W,W},
                {E,E,E,E,E}
    };

    private static final byte[][] BELGIAN_DAISY_LAYOUT = {
                {W,W,E,B,B},
               {W,W,W,B,B,B},
              {E,W,W,E,B,B,E},
             {E,E,E,E,E,E,E,E},
            {E,E,E,E,E,E,E,E,E},
             {E,E,E,E,E,E,E,E},
              {E,B,B,E,W,W,E},
               {B,B,B,W,W,W},
                {B,B,E,W,W}
    };

    private static final String[][] CONFORMANCE_COORDINATES = {
                        {"I5", "I6", "I7", "I8", "I9"},
                     {"H4", "H5", "H6", "H7", "H8", "H9"},
                  {"G3", "G4", "G5", "G6", "G7", "G8", "G9"},
               {"F2", "F3", "F4", "F5", "F6", "F7", "F8", "F9"},
            {"E1", "E2", "E3", "E4", "E5", "E6", "E7", "E8", "E9"},
               {"D1", "D2", "D3", "D4", "D5", "D6", "D7", "D8"},
                  {"C1", "C2", "C3", "C4", "C5", "C6", "C7"},
                     {"B1", "B2", "B3", "B4", "B5", "B6"},
                        {"A1", "A2", "A3", "A4", "A5"}
    };

    /// Cached coordinates. Use this instead of making "new".
    public static final Coordinate[][] COORDINATES = initCoordinates(STANDARD_LAYOUT);

    /// Maps each coordinate to it's neighbors
    private static final Neighbors[][] COORDINATE_NEIGHBORS = initNeighbors(STANDARD_LAYOUT, COORDINATES);

    /// Maps requirements-conforming coordinates to our coordinates
    private static final Map<String, Coordinate> CONF_COORD_MAP = initCoordMap(CONFORMANCE_COORDINATES, COORDINATES);

    private static Map<String, Coordinate> initCoordMap(String[][] mapping, Coordinate[][] coordCache) {
        Map<String, Coordinate> cm = new HashMap<>();
        for (int row = 0; row < mapping.length; ++row) {
            for (int col = 0; col < mapping[row].length; ++col) {
                cm.put(mapping[row][col], coordCache[row][col]);
            }
        }
        return cm;
    }

    private static Neighbors[][] initNeighbors(byte[][] referenceBoard, Coordinate[][] coordCache) {
        Neighbors[][] neighbors = new Neighbors[referenceBoard.length][];
        for (int row = 0; row < neighbors.length; ++row) {
            neighbors[row] = new Neighbors[referenceBoard[row].length];
            for (int col = 0; col < neighbors[row].length; ++col)
                neighbors[row][col] = findNeighbors(referenceBoard, coordCache, coordCache[row][col]);
        }
        return neighbors;
    }

    private static Neighbors findNeighbors(byte[][] referenceBoard, Coordinate[][] coordCache, Coordinate center) {
        Coordinate[] coords = new Coordinate[6]; // in order NW, W, SW, SE, E, NE
        for (int row = Math.max(0, center.y - 1); row <= Math.min(center.y + 1, referenceBoard.length - 1); ++row) {
            for (int col = Math.max(0, center.x - 1); col <= Math.min(center.x + 1, referenceBoard[row].length - 1); ++col) {
                if (areNeighbors(coordCache[row][col], center)) {
                    Direction dir = findNeighborDirection(center, coordCache[row][col]);
                    coords[dir.ordinal()] = coordCache[row][col];
                }
            }
        }
        return new Neighbors(coords[0], coords[1], coords[2], coords[3], coords[4], coords[5]);
    }

    private static Coordinate[][] initCoordinates(byte[][] referenceBoard) {
        Coordinate[][] coordinates = new Coordinate[referenceBoard.length][];
        for (int row = 0; row < coordinates.length; ++row) {
            coordinates[row] = new Coordinate[referenceBoard[row].length];
            for (int col = 0; col < coordinates[row].length; ++col)
                coordinates[row][col] = new Coordinate(col, row);
        }
        return coordinates;
    }

    // translate x coordinate to match a square matrix (board padded on top left and bottom right)
    private static int _trx(int x, int y) { return Math.max(4 - y + x, x); }

    // Finds the coordinate between two other coordinates
    // if can't find one (nothing in between, too far apart, not on the same axis), return null
    public static Coordinate findCoordBetween(Coordinate a, Coordinate b) {
        int dx = Math.abs(a.x - b.x);
        if (a.y == b.y && dx == 2) { // same row
            return COORDINATES[a.y][Math.min(a.x, b.x) + 1];
        } else if (Math.abs(a.y - b.y) == 2) { // different rows
            int yBetween = Math.min(a.y, b.y) + 1;
            if (a.x == b.x) // on the same diagonal, not crossing row 4
                return COORDINATES[yBetween][a.x];
            if (dx == 2) // also same diagonal, not crossing row 4
                return COORDINATES[yBetween][Math.min(a.x, b.x) + 1];
            if (dx == 1 && yBetween == 4) // crossing row 4
                return COORDINATES[yBetween][Math.max(a.x, b.x)];
        }
        return null;
    }

    public static boolean areNeighbors(Coordinate a, Coordinate b) {
        int dx = _trx(a.x, a.y) - _trx(b.x, b.y);
        int dy = a.y - b.y;
        int absDx = Math.abs(dx);
        //     Manhattan dist = 1       OR (1 x away AND on bottom-left to top-right diagonal)
        return absDx + Math.abs(dy) == 1 | absDx == 1 & dx + dy == 0;
    }

    public static boolean areNeighbors(Coordinate a, Coordinate b, Coordinate c) {
        // this allows passing the same coordinate into two parameters, so for example
        // if a == c, this will return true.. should it?
        // perhaps we should check here if (a.equals(b) || a.equals(c) || b.equals(c))

        if (areNeighbors(a, b)) // abc, bac, cab, cba
            return areNeighbors(b, c) || areNeighbors(c, a);

        // acb, bca
        return areNeighbors(a, c) && areNeighbors(b, c);
    }

    // Assumes the two coordinates are neighbors, does not do additional checking
    public static Direction findNeighborDirection(Coordinate from, Coordinate to) {
        int fromTrx = _trx(from.x, from.y);
        int toTrx = _trx(to.x, to.y);

        if (from.y < to.y)
            return fromTrx == toTrx ? Direction.SE : Direction.SW;
        if (from.y > to.y)
            return fromTrx == toTrx ? Direction.NW : Direction.NE;

        return fromTrx < toTrx ? Direction.E : Direction.W;
    }

    // Checks if the 3 coordinates are on one of the 3 axes that are valid in the context of this game
    public static boolean onSameAxis(Coordinate a, Coordinate b, Coordinate c) {
        int trxA = _trx(a.x, a.y);
        int trxB = _trx(b.x, b.y);
        int trxC = _trx(c.x, c.y);

        return trxA - trxB + a.y - b.y == 0 & trxA - trxC + a.y - c.y == 0
             | trxA == trxB & trxA == trxC
             | a.y == b.y & a.y == c.y;
    }

    public static String toConformanceCoord(Coordinate coord) {
        return CONFORMANCE_COORDINATES[coord.y][coord.x];
    }

    public static String toConformanceCoord(int x, int y) {
        return CONFORMANCE_COORDINATES[y][x];
    }

    public static Coordinate toCoord(String coord) {
        return CONF_COORD_MAP.get(coord);
    }

    public static Neighbors neighborsOf(Coordinate coord) {
        return COORDINATE_NEIGHBORS[coord.y][coord.x];
    }

    public static Board makeStandardLayout(double height) {
        return new Board(deepCopyRepresentation(STANDARD_LAYOUT), height);
    }

    public static Board makeGermanDaisy(double height) {
        return new Board(deepCopyRepresentation(GERMAN_DAISY_LAYOUT), height);
    }

    public static Board makeBelgianDaisy(double height) {
        return new Board(deepCopyRepresentation(BELGIAN_DAISY_LAYOUT), height);
    }

    public static byte[][] deepCopyRepresentation(byte[][] original) {

        final byte[][] result = new byte[original.length][];
        for (int i = 0; i < original.length; i++) {
            result[i] = Arrays.copyOf(original[i], original[i].length);
        }
        return result;
    }
}
