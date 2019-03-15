import java.util.HashMap;

public class BoardUtil {
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

    private static final HashMap<String, Board.Coordinates> coordMap = initCoordMap();

    private static HashMap<String, Board.Coordinates> initCoordMap() {
        HashMap<String, Board.Coordinates> cm = new HashMap<>();
        for (int y = 0; y < CONFORMANCE_COORDINATES.length; ++y) {
            for (int x = 0; x < CONFORMANCE_COORDINATES[y].length; ++x) {
                cm.put(CONFORMANCE_COORDINATES[y][x], new Board.Coordinates(x, y));
            }
        }
        return cm;
    }

    public static String toConformanceCoord(Board.Coordinates coord) {
        return CONFORMANCE_COORDINATES[coord.y][coord.x];
    }

    public static String toConformanceCoord(int x, int y) {
        return CONFORMANCE_COORDINATES[y][x];
    }

    public static Board.Coordinates toCoord(String coord) {
        return coordMap.get(coord);
    }

    public static Board makeStandardLayout() {
        return new Board(STANDARD_LAYOUT);
    }

    public static Board makeGermanDaisy() {
        return new Board(GERMAN_DAISY_LAYOUT);
    }

    public static Board makeBelgianDaisy() {
        return new Board(BELGIAN_DAISY_LAYOUT);
    }
}
