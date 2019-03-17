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
        public final Board.Coordinate coordinate;
        public final Direction direction;

        public static Neighbor makeNeighbor(Board.Coordinate coordinate, Direction direction) {
            if (coordinate == null || direction == null)
                return null;
            return new Neighbor(coordinate, direction);
        }

        private Neighbor(Board.Coordinate coordinate, Direction direction) {
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

        Neighbors(Board.Coordinate NW,
                  Board.Coordinate W,
                  Board.Coordinate SW,
                  Board.Coordinate SE,
                  Board.Coordinate E,
                  Board.Coordinate NE)
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

        public Neighbor fromCoordinate(Board.Coordinate coord) {
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
    public static final Board.Coordinate[][] COORDINATES = initCoordinates(STANDARD_LAYOUT);

    /// Maps each coordinate to it's neighbors
    private static final Neighbors[][] COORDINATE_NEIGHBORS = initNeighbors(STANDARD_LAYOUT, COORDINATES);

    /// Maps requirements-conforming coordinates to our coordinates
    private static final Map<String, Board.Coordinate> CONF_COORD_MAP = initCoordMap(CONFORMANCE_COORDINATES, COORDINATES);

    private static Map<String, Board.Coordinate> initCoordMap(String[][] mapping, Board.Coordinate[][] coordCache) {
        Map<String, Board.Coordinate> cm = new HashMap<>();
        for (int row = 0; row < mapping.length; ++row) {
            for (int col = 0; col < mapping[row].length; ++col) {
                cm.put(mapping[row][col], coordCache[row][col]);
            }
        }
        return cm;
    }

    private static Neighbors[][] initNeighbors(byte[][] referenceBoard, Board.Coordinate[][] coordCache) {
        Neighbors[][] neighbors = new Neighbors[referenceBoard.length][];
        for (int row = 0; row < neighbors.length; ++row) {
            neighbors[row] = new Neighbors[referenceBoard[row].length];
            for (int col = 0; col < neighbors[row].length; ++col)
                neighbors[row][col] = findNeighbors(referenceBoard, coordCache, coordCache[row][col]);
        }
        return neighbors;
    }

    private static Neighbors findNeighbors(byte[][] referenceBoard, Board.Coordinate[][] coordCache, Board.Coordinate center) {
        Board.Coordinate[] coords = new Board.Coordinate[6]; // in order NW, W, SW, SE, E, NE
        for (int row = Math.max(0, center.y - 1); row <= Math.min(center.y + 1, referenceBoard.length - 1); ++row) {
            for (int col = Math.max(0, center.x - 1); col <= Math.min(center.x + 1, referenceBoard[row].length - 1); ++col) {
                // in the top half, bottom half and the middle of the hexagon, we have to use different ways to exclude points that are too far
                // e.g. (0,0) (0,1) (x,x) exclude from bottom left to top right
                //      (1,0) (x,x) (1,2)
                //      (x,x) (2,1) (2,2)
                //-----------------------
                //      (x,x) (5,2) (5,3) exclude from bottom right to top left
                //      (6,1) (x,x) (6,3)
                //      (7,1) (7,2) (x,x)
                boolean notBLTRDiagonal = row - center.y + col - center.x != 0;
                boolean notBRTLDiagonal = row - center.y !=  col - center.x;
                boolean beforeMid = row < 4 || (row == 4 && center.y <= 4);
                boolean afterMid  = row > 4 || (row == 4 && center.y > 4);
                // simplify..?
                if ((notBLTRDiagonal && beforeMid) || (notBRTLDiagonal && afterMid)) {
                    boolean westSide = col < center.x || (col == center.x && (afterMid && row < center.y || beforeMid && row > center.y));
                    if (row < center.y) {
                        coords[westSide ? 0 : 5] = coordCache[row][col];
                    } else if (row == center.y) {
                        coords[westSide ? 1 : 4] = coordCache[row][col];
                    } else /* row > coord.y */ {
                        coords[westSide ? 2 : 3] = coordCache[row][col];
                    }
                }
            }
        }
        return new Neighbors(coords[0], coords[1], coords[2], coords[3], coords[4], coords[5]);
    }

    private static Board.Coordinate[][] initCoordinates(byte[][] referenceBoard) {
        Board.Coordinate[][] coordinates = new Board.Coordinate[referenceBoard.length][];
        for (int row = 0; row < coordinates.length; ++row) {
            coordinates[row] = new Board.Coordinate[referenceBoard[row].length];
            for (int col = 0; col < coordinates[row].length; ++col)
                coordinates[row][col] = new Board.Coordinate(col, row);
        }
        return coordinates;
    }

    public static String toConformanceCoord(Board.Coordinate coord) {
        return CONFORMANCE_COORDINATES[coord.y][coord.x];
    }

    public static String toConformanceCoord(int x, int y) {
        return CONFORMANCE_COORDINATES[y][x];
    }

    public static Board.Coordinate toCoord(String coord) {
        return CONF_COORD_MAP.get(coord);
    }

    public static Neighbors neighborsOf(Board.Coordinate coord) {
        return COORDINATE_NEIGHBORS[coord.y][coord.x];
    }

    public static Board makeStandardLayout(double height) {
        return new Board(STANDARD_LAYOUT, height);
    }

    public static Board makeGermanDaisy(double height) {
        return new Board(GERMAN_DAISY_LAYOUT, height);
    }

    public static Board makeBelgianDaisy(double height) {
        return new Board(BELGIAN_DAISY_LAYOUT, height);
    }
}
