import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class TranspositionTable {
    private static Map<HashableState, TableEntry> transpostion = new HashMap<>();
    public static final int WHITE_SEED = 0;
    public static final int BLACK_SEED = 1;
    public static final int RANGE = 100000;
    private static long[][][] table = initializeZobrist();;


    public static long generateZobristKey(byte[][] board) {
        long hash = 0;
        for (int i = 0; i<board.length; i++) {
            for (int j = 0; j<board[i].length; j++) {
                if (board[i][j] != 'E') {
                    int piece = board[i][j]=='W' ? WHITE_SEED : BLACK_SEED;
                    hash ^= table[i][j][piece];
                }
            }
        }
        return hash;
    }

    private static long[][][] initializeZobrist() {
        long[][][] table = new long[9][9][2];
        for (int i = 0; i<table.length; i++) {
            for (int j = 0; j<table[i].length; j++) {
                for (int k = 0; k<table[i][j].length; k++) {
                    table[i][j][k] = (long) (Math.random()*RANGE);
                }
            }
        }
        return table;
    }

    public static TableEntry get(byte[][] board, byte maxPlayer) {
        long key = TranspositionTable.generateZobristKey(board);
        HashableState hashicorp = new HashableState(key, maxPlayer);
        return transpostion.get(hashicorp);
    }

    public static void put(byte[][] board, byte maxPlayer, TableEntry entry) {
        long key = TranspositionTable.generateZobristKey(board);
        HashableState hashicorp = new HashableState(key, maxPlayer);
        transpostion.put(hashicorp, entry);
    }

    public static void clear() { transpostion.clear(); }

    private static class HashableState {
        final long zKey;
        final byte maxPlayer;

        public HashableState(long zKey, byte maxPlayer) {
            this.maxPlayer = maxPlayer;
            this.zKey = zKey;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            HashableState that = (HashableState) o;
            return zKey == that.zKey &&
                    maxPlayer == that.maxPlayer;
        }

        @Override
        public int hashCode() {
            return Objects.hash(zKey, maxPlayer);
        }
    }
}
