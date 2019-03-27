import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
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
     * @param inputFile the test file number as a string
     */
    public static void readInputFile(String inputFile) {
        copyEmptyBoard();
        File file = new File(inputFile);
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
        StringBuilder whites = new StringBuilder();
        StringBuilder blacks = new StringBuilder();
        //loop from rows 8 -> 0 for alphabetical ordering
        for (int i = board.length-1; i >= 0; i--) {
            for (int j = 0; j < board[i].length; j++){
                switch(board[i][j]) {
                    case E:
                        //skips because board file has no empty representation
                        break;
                    case W:
                        whites.append(BoardUtil.toConformanceCoord(j,i)+"w,");
                        break;
                    case B:
                        blacks.append(BoardUtil.toConformanceCoord(j,i)+"b,");
                        break;
                    default:
                }
            }
        }
        blacks.append(whites.toString());
        //removes comma at the end
        return blacks.substring(0,blacks.length()-1);
    }

    public static void main(String[] args) {
        if (args.length!=0) {
            System.out.println(args[0]);
            File[] files = new File(args[0]).listFiles();
            for (int i = 0; i<files.length; i++) {
                if (files[i].getName().split("\\.")[1].equals("input")) {
                    System.out.println(files[i].getName());
                    TestUtil.readInputFile(args[0]+files[i].getName());
                    System.out.println("Processing "+files[i].getName());
                    // Add movegenerator call here for BOARD to populate the curOutput field
                    String boardConf = TestUtil.boardConfigToStringRep(getTestConfigBoard());

                    // Add movegenerator call here for MOVE to populate the curOutput field
                    String moveConf = TestUtil.boardConfigToStringRep(getTestConfigBoard());
                    try {
                        generateBoardFile(args[0]+files[i].getName(), boardConf);
                        generateMoveFile(args[0]+files[i].getName(), moveConf);
                    } catch(IOException e) {
                        System.out.println(e.getMessage());
                    }
                }
            }
        }
    }

    public static void generateBoardFile(String testFileName, String contents) throws IOException {
        String newFileName = testFileName.split("\\.")[0];
        try {
            FileWriter fout = new FileWriter(newFileName+".board");
            fout.write(contents);
            fout.close();
        } catch(IOException e) {
            System.out.println(e.getMessage());
        }
    }

    public static void generateMoveFile(String testFileName, String contents) {
        String newFileName = testFileName.split("\\.")[0];
        try {
            FileWriter fout = new FileWriter(newFileName+".move");
            fout.write(contents);
            fout.close();
        } catch(IOException e) {
            System.out.println(e.getMessage());
        }
    }

    public static byte[][] getTestConfigBoard(){
        return TEST_CONFIG;
    }
}
