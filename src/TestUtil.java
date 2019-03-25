import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class TestUtil {
    private static final byte E = Board.EMPTY, W = Board.WHITE, B = Board.BLACK;
    private static byte player;
    private BoardUtil boardUtil;
    private static byte[][] TEST_CONFIG;


    //Empty board will be filled with test board config
    private static final byte[][] EMPTY_BOARD = {
                {E,E,E,E,E},
               {E,E,E,E,E,E},
              {E,E,E,E,E,E,E},
             {E,E,E,E,E,E,E,E},
            {E,E,E,E,E,E,E,E,E},
             {E,E,E,E,E,E,E,E},
              {E,E,E,E,E,E,E},
               {E,E,E,E,E,E},
                {E,E,E,E,E}
    };

    public TestUtil() {

    }

    /**
     *
     * deep copy EMPTY_BOARD to fill with a test board configuration.
     */
    private static void copyEmptyBoard() {
        TEST_CONFIG = new byte[EMPTY_BOARD.length][];
        for (int i = 0; i < EMPTY_BOARD.length; i++) {
            TEST_CONFIG[i] = new byte[EMPTY_BOARD[i].length];
        }
        for (int i = 0; i < EMPTY_BOARD.length; i++) {
            System.arraycopy(EMPTY_BOARD[i], 0, TEST_CONFIG[i], 0, EMPTY_BOARD[i].length);
        }
    }

    private static void addToTestConfigBoard(String conformCoord, String color) {
        Coordinate coord = BoardUtil.toCoord(conformCoord);
        TEST_CONFIG[coord.y][coord.x] = (byte)color.charAt(0);
    }

    /**
     * reads the test file and maps test board configuration to TEST_CONFIG
     * sets the current player W or B to a byte
     * @param fileName the test file number as a string
     */
    public static void readInputFile(String fileName) {
        //fills TEST_CONFIG with all E
        copyEmptyBoard();
        File file = new File("src/test/" + fileName );
        try {
            Scanner scan = new Scanner(file);
            while (scan.hasNext()) {
                player = (byte)scan.next().toUpperCase().charAt(0);
                Scanner boardScanner = new Scanner (scan.next().trim()).useDelimiter(",");
                while(boardScanner.hasNext()) {
                    String coordAndColor = boardScanner.next();
                    String coord = coordAndColor.substring(0, 2);
                    String color = coordAndColor.substring(2).toUpperCase();
                    addToTestConfigBoard(coord, color);
                }
                boardScanner.close();
            }
        } catch (FileNotFoundException e) {
            System.err.println("failed to read file");
            e.printStackTrace();
        }
    }

    /**
     *
     * @return the current player of TEST_CONFIG
     */
    public static byte getCurTestPlayer() {
        return player;
    }

    /**
     * for testing purposes, prints byte board representation of test file read
     */
    public static void printTestConfigBoard() {
        if (TEST_CONFIG != null) {
            for (int i = 0; i < TEST_CONFIG.length; i++) {
                for(int j = 0; j < TEST_CONFIG[i].length; j++) {
                    System.out.print(TEST_CONFIG[i][j] + " ");
                }
                System.out.println("");
            }
        } else {
            System.out.println("test config is null");
        }
    }

    /**
     * for test purposes, can print to check byte[][] board
     * @param board byte[][] board representation
     */
    public static void printBoard(byte[][] board) {
        for (int i = 0; i < board.length; i++) {
            for(int j = 0; j < board[i].length; j++) {
                System.out.print(board[i][j] + " ");
            }
            System.out.println("");
        }
    }

    public static boolean compareBoardConfigs(byte[][] testBoard1, byte[][] testBoard2) {
        //checks equality of each element in testBoard1 and testBoard2
        for (int i = 0; i < testBoard1.length; i++) {
            for (int j = 0; j < testBoard1[i].length; j++) {
                if (testBoard1[i][j] != testBoard2[i][j]) {
                    return false;
                }
            }
        }
        return true;
    }

    public static String boardConfigToStringRep(byte[][] board) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++){
            }
        }
        return "test";
    }

    public static void main(String[] args) {
        Scanner userInput = new Scanner(System.in);
        System.out.println("Enter an input file name to test Ex: Test1.input");
        String testFile = userInput.nextLine();
        readInputFile(testFile);
        TestUtil.printTestConfigBoard();
    }

    public static byte[][] getTestConfigBoard(){
        return TEST_CONFIG;
    }
}
