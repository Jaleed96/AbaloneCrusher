import org.omg.CORBA.TRANSACTION_MODE;

import java.util.HashMap;
import java.util.Map;

public class TranspositionTable {
    private static Map<Long, TableEntry> transpostion = new HashMap<>();
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

    public static boolean containsKey(long key) {
        return transpostion.containsKey(key);
    }
    public static boolean containsKey(byte[][] board) {
        long key = TranspositionTable.generateZobristKey(board);
        return transpostion.containsKey(key);
    }

    public static TableEntry get(long key) {
        return transpostion.get(key);
    }
    public static TableEntry get(byte[][] board) {
        long key = TranspositionTable.generateZobristKey(board);
        return transpostion.get(key);
    }
    public static void put(long key, TableEntry entry) {
        transpostion.put(key, entry);
    }
    public static void put(byte[][] board, TableEntry entry) {
        long key = TranspositionTable.generateZobristKey(board);
        transpostion.put(key, entry);
    }
}
