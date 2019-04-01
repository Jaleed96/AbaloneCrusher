import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Scanner;
import java.util.StringJoiner;

public class TestUtil {
    private static final byte E = Board.EMPTY;

    private static class TestConfig {
        byte player;
        byte[][] board;
    }

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

    private static void addToTestConfigBoard(byte[][] testBoard, String conformCoord, String color) {
        Coordinate coord = BoardUtil.toCoord(conformCoord);
        testBoard[coord.y][coord.x] = (byte) color.charAt(0);
    }

    /**
     * reads the test boardFile and maps test board configuration to a test config
     * sets the current player W or B to a byte
     * @param boardFile the .board file
     */
    private static TestConfig readBoardFile(File boardFile) throws FileNotFoundException {
        TestConfig test = new TestConfig();
        test.board = BoardUtil.deepCopyRepresentation(EMPTY_BOARD);
        try (Scanner scan = new Scanner(boardFile)) {
            while (scan.hasNext()) {
                test.player = (byte)scan.next().toUpperCase().charAt(0);
                if (scan.hasNext()) {
                    Scanner boardScanner = new Scanner(scan.next().trim()).useDelimiter(",");
                    while (boardScanner.hasNext()) {
                        String coordAndColor = boardScanner.next().trim();
                        String coord = coordAndColor.substring(0, 2).toUpperCase();
                        String color = coordAndColor.substring(2).toUpperCase();
                        addToTestConfigBoard(test.board, coord, color);
                    }
                    boardScanner.close();
                }
            }
            return test;
        }
    }

    /**
     * for test purposes, can print to check byte[][] board
     * @param board byte[][] board representation
     */
    public static void printBoard(byte[][] board) {
        for (int row = 0; row < board.length; row++) {
            for(int col = 0; col < board[row].length; col++) {
                if (col != 0)
                    System.out.print(" ");
                System.out.print((char) board[row][col]);
            }
            System.out.println();
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
        for (int row = board.length - 1; row >= 0; row--) {
            for (int col = 0; col < board[row].length; col++){
                switch(board[row][col]) {
                    case Board.EMPTY:
                        //skips because board file has no empty representation
                        break;
                    case Board.WHITE:
                        whites.append(BoardUtil.toConformanceCoord(col, row)).append("w,");
                        break;
                    case Board.BLACK:
                        blacks.append(BoardUtil.toConformanceCoord(col, row)).append("b,");
                        break;
                }
            }
        }
        blacks.append(whites.toString());
        //removes comma at the end
        return blacks.substring(0, Math.max(0, blacks.length() - 1));
    }

    public static void main(String[] args) {
        if (args.length != 0) {
            File path = new File(args[0]);
            if (path.exists()) {
                if (path.isDirectory()) {
                    File[] files = path.listFiles();
                    for (File file : files) {
                        if (isInputFile(file)) {
                            processInputFile(file, path.toPath());
                        } else {
                            System.out.println("Skipping " + file.getName());
                        }
                    }
                } else if (isInputFile(path)) {
                    processInputFile(path, path.toPath().getParent());
                } else {
                    System.out.println("Cannot process input: " + path);
                }
            } else {
                System.out.println("Path " + path + " doesn't exist.");
            }
        } else {
            System.out.println("Please specify a .input file or a directory containing .input files as the first argument.");
        }
    }

    private static void processInputFile(File inputFile, Path outPath) {
        System.out.println("Processing " + inputFile.getName() + "...");
        TestConfig test;
        try {
            test = TestUtil.readBoardFile(inputFile);
        } catch (FileNotFoundException e) {
            System.err.println(e.getMessage());
            return;
        }

        byte opponent = Board.playersOpponent(test.player);
        if (opponent == Board.EMPTY) {
            System.err.println("Invalid player: " + (char) test.player);
            return;
        }

        List<OrderedMove> legalMoves = MoveGenerator.generate(test.board, test.player, opponent);

        String boardResultDate = formatBoardOutput(test.board, legalMoves);
        String moveResultData = formatMoveOutput(legalMoves);

        try {
            String boardFilename = generateTestOutputFile(outPath, inputFile.getName(), "board", boardResultDate);
            String moveFilename = generateTestOutputFile(outPath, inputFile.getName(), "move", moveResultData);
            System.out.println(String.format("Done. %s and %s created in %s", boardFilename, moveFilename, outPath));
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    private static String formatMoveOutput(List<OrderedMove> moves) {
        StringJoiner result = new StringJoiner(System.lineSeparator());
        for (OrderedMove m : moves) {
            result.add(MoveParser.toText(m.move));
        }
        return result.toString();
    }

    private static String formatBoardOutput(byte[][] board, List<OrderedMove> moves) {
        StringJoiner result = new StringJoiner(System.lineSeparator());
        for (OrderedMove m : moves) {
            byte[][] afterMove = BoardUtil.copyThenApply(board, m.move);
            result.add(boardConfigToStringRep(afterMove));
        }
        return result.toString();
    }

    // returns new filename
    private static String generateTestOutputFile(Path outPath, String testFileName, String newFileExtension, String contents) throws IOException {
        String newFileName = testFileName.substring(0,testFileName.length() - ".input".length())  + "." + newFileExtension;
        try (FileWriter fout = new FileWriter(outPath.resolve(newFileName).toString())) {
            fout.write(contents);
        }
        return newFileName;
    }

    private static boolean isInputFile(File file) {
        return file.isFile() && file.getName().toLowerCase().endsWith(".input");
    }
}
