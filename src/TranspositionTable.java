import org.omg.CORBA.TRANSACTION_MODE;

import java.util.HashMap;
import java.util.Map;

public class TranspositionTable {
    private Map<Long, TableEntry> transpostion = new HashMap<>();
    public static final int WHITE_SEED = 1;
    public static final int BLACK_SEED = 2;
    public static final int RANGE = 100000;
    private static final int[][][] table = new int[9][9][2];;


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

    private static void initializeZobrist() {
        for (int i = 0; i<table.length; i++) {
            for (int j = 0; j<table[i].length; i++) {
                for (int k = 0; k<table[i][j].length; k++) {
                    table[i][j][k] = (int) (Math.random()*RANGE);
                }
            }
        }
    }

    public boolean containsKey(long key) {
        return transpostion.containsKey(key);
    }
    public boolean containsKey(byte[][] board) {
        long key = TranspositionTable.generateZobristKey(board);
        return transpostion.containsKey(key);
    }

    public TableEntry get(long key) {
        return transpostion.get(key);
    }
    public TableEntry get(byte[][] board) {
        long key = TranspositionTable.generateZobristKey(board);
        return transpostion.get(key);
    }
}
