import java.util.HashMap;

public class Board {
    public static final byte EMPTY = 'E', WHITE = 'W', BLACK = 'B';

    public static class Coordinates {
        public final int x;
        public final int y;

        Coordinates(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

    private byte[][] board;

    Board(byte[][] board) {
        this.board = board;
    }
}
