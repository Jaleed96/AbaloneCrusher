public class Board {
    public static final byte EMPTY = 'E', WHITE = 'W', BLACK = 'B';

    private byte[][] board;

    public static Board makeStandardLayout() {
        byte[][] layout = {
                              {WHITE, WHITE, WHITE, WHITE, WHITE},
                           {WHITE, WHITE, WHITE, WHITE, WHITE, WHITE},
                       {EMPTY, EMPTY, WHITE, WHITE, WHITE, EMPTY, EMPTY},
                    {EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY},
                {EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY},
                    {EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY},
                       {EMPTY, EMPTY, BLACK, BLACK, BLACK, EMPTY, EMPTY},
                           {BLACK, BLACK, BLACK, BLACK, BLACK, BLACK},
                               {BLACK, BLACK, BLACK, BLACK, BLACK}
        };
        return new Board(layout);
    }

    public static Board makeGermanDaisy() {
        return null;

    }

    public static Board makeBelgianDaisy() {
        return null;

    }

    private Board(byte[][] board) {
        this.board = board;
    }
}
