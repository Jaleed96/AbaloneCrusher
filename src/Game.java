import java.util.Stack;
import java.util.Timer;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class Game {
    private boolean GAME_STOPPED;
    // USE GAME_PAUSED to check whether game is in session
    private boolean GAME_PAUSED;
    private Scene scene;
    private Stage stage;
    private Button newGameBtn, confirmBtn, resetBtn, stopBtn, undoBtn, pauseBtn;
    private Label blackScoreLabel;
    private Label blackScore;
    private Label whiteScoreLabel;
    private Label currentPlayer;
    private Label whiteScore;
    private Label timeLabel;
    private Label currentPlayerHistory = new Label("Black");
    private TextField moveInput;
    private Label gameState;
    private Label movesLeftW;
    private Label movesLeftB;
    public Label history;
    private Label endGameMsg;
    private Timer timer;
    private CheckBox toggleCoordOverlay;
    private int turn = 1;
    private int timeLeftCount;
    private Gamestate lastGamestate;
    private Stack<Integer> totalBlackTime = new Stack<>();
    private Stack<Integer> totalWhiteTime = new Stack<>();

    Game(Config cfg, double w, double h, Scene menuScene, Stage stage) {
        this.stage = stage;
        // BorderPane rootLayout = new BorderPane();
        HBox rootLayout = new HBox();
        rootLayout.setPrefSize(w, h);
        // VBOXButton pane on left
        VBox leftPane = new VBox(50);

        currentPlayer = new Label("Turn: Black");

        // HBox for white marble scoring
        HBox blackScoreRow = new HBox();
        blackScoreLabel = new Label("Black: ");
        blackScore = new Label("0");
        blackScoreRow.getChildren().addAll(blackScoreLabel, blackScore);

        // VBox for black marble scoring
        HBox whiteScoreRow = new HBox();
        whiteScoreLabel = new Label("White: ");
        whiteScore = new Label("0");
        whiteScoreRow.getChildren().addAll(whiteScoreLabel, whiteScore);

        newGameBtn = new Button("New Game");
        resetBtn = new Button("Reset Game");
        stopBtn = new Button("Stop");
        toggleCoordOverlay = new CheckBox("Show coordinates");
        toggleCoordOverlay.setSelected(true);

        // VBOX Board, clock and input field in the center
        VBox centerPane = new VBox(50);
        HBox topRow = new HBox(50);

        pauseBtn = new Button("Resume/Pause");
        gameState = new Label();
        timeLabel = new Label(Integer.toString(cfg.blackTimeLimitMs));
        topRow.getChildren().addAll(timeLabel, pauseBtn, gameState);

        HBox bottomRow = new HBox(50);
        moveInput = new TextField();
        confirmBtn = new Button("Confirm");

        undoBtn = new Button("Undo last move");
        bottomRow.getChildren().addAll(moveInput, confirmBtn, undoBtn);

        final Board gameBoard;
        double boardHeight = 500;
        switch (cfg.initialLayout) {
            case Standard:
                gameBoard = BoardUtil.makeStandardLayout(boardHeight, cfg);
                break;
            case GermanDaisy:
                gameBoard = BoardUtil.makeGermanDaisy(boardHeight, cfg);
                break;
            case BelgianDaisy:
                gameBoard = BoardUtil.makeBelgianDaisy(boardHeight, cfg);
                break;
            default:
                gameBoard = null;
                break;
        }

        GAME_PAUSED = gameBoard.GAME_PAUSED;
        GAME_STOPPED = gameBoard.GAME_STOPPED;

        gameBoard.addTimeUpdatedListener((player, timeLeft) -> {
            timeLeftCount = timeLeft;
            timeLabel.setText(String.format("%d.%03d s", timeLeft / 1000, timeLeft % 1000));
        });

        gameBoard.setGameInSessionListener((winner, winType) -> {
            GAME_STOPPED = gameBoard.GAME_STOPPED;
            if (winner == null) {
                gameState.setText("Tie: player scores are equal");
                gameState.setText(String.format("%s\n%s %3.2fs\n%s %3.2fs", "Game Stopped", "Total Black Time:", ((double)getTotalTime(totalBlackTime) / 1000)
                        , "Total White Time:", ((double)getTotalTime(totalWhiteTime) / 1000)));
            } else {
                gameState.setText((char) winner.piece+" wins"+": "+winType);
            }
        });

        movesLeftB = new Label("Moves Left (Black): " + gameBoard.blackMovesLeft);
        movesLeftW = new Label("Moves Left (White): " + gameBoard.whiteMovesLeft);
        leftPane.getChildren().addAll(movesLeftB, movesLeftW, currentPlayer, blackScoreRow, whiteScoreRow, newGameBtn,
                resetBtn, stopBtn, toggleCoordOverlay);
        gameBoard.gui().setTextCoordVisibility(toggleCoordOverlay.isSelected());
        toggleCoordOverlay.selectedProperty().addListener((observable, wasChecked, isChecked) -> {
            gameBoard.gui().setTextCoordVisibility(isChecked);
        });

        gameBoard.setPastGameStateListener((gamestate, move) -> {
            lastGamestate = gamestate;
            history.setText(String.format(("%s%s.(%s) %s (%3.2fs)\n"), history.getText(), String.valueOf(turn),
                    currentPlayerHistory.getText(), MoveParser.toText(move),
                    ((double) (gameBoard.currentPlayer().getTimeLimitMs() - timeLeftCount) / 1000)));
            if (gameBoard.currentPlayer().piece == 'B') {
               totalBlackTime.push(((gameBoard.currentPlayer().getTimeLimitMs() - timeLeftCount)));
            } else {
                totalWhiteTime.push((gameBoard.currentPlayer().getTimeLimitMs() - timeLeftCount));
            }
            turn++;
        });

        gameBoard.setScoreUpdateListener((blackPlayer, whitePlayer) -> {
            whiteScore.setText(Integer.toString(whitePlayer.score()));
            blackScore.setText(Integer.toString(blackPlayer.score()));
        });

        gameBoard.setCurrentPlayerChangedListener(player -> {
            switch (player.piece) {
            case Board.WHITE:
                movesLeftB.setText("Moves Left (Black): " + Integer.toString(gameBoard.blackMovesLeft));
                currentPlayer.setText("Turn: White");
                currentPlayerHistory.setText("White");
                break;
            case Board.BLACK:
                movesLeftW.setText("Moves Left (White): " + Integer.toString(gameBoard.whiteMovesLeft));
                currentPlayer.setText("Turn: Black");
                currentPlayerHistory.setText("Black");
                break;
            }
        });

        confirmBtn.setOnAction(e -> {
            if (!GAME_PAUSED && !GAME_STOPPED) {
                Move move = null;
                try {
                    move = MoveParser.parse(moveInput.getText());
                } catch (Exception ex) {
                    // TODO display in a text field
                    System.out.println(ex.getMessage());
                    return;
                }

                try {
                    gameBoard.makeMove(move);
                } catch (Move.IllegalMoveException ex) {
                    // TODO display in a text field
                    System.out.println(ex.getMessage());
                }
            }
        });

        MoveSelection moveSelection = new MoveSelection(gameBoard);
        moveSelection.setOnMoveSelectedListener(move -> {
            if (!GAME_PAUSED && !GAME_STOPPED) {
                try {
                    System.out.println("Saving state");
                    // saves the current state of board before a move is made
                    gameBoard.makeMove(move);
                } catch (Move.IllegalMoveException e) {
                    System.out.println(e.getMessage());
                }
            }
        });

        centerPane.getChildren().addAll(topRow, gameBoard.gui(), bottomRow);

        // VBOX History box on the right
        VBox rightPane = new VBox(50);
        history = new Label();
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setContent(rightPane);
        scrollPane.setPannable(true);
        rightPane.getChildren().addAll(history);
        rootLayout.getChildren().addAll(leftPane, centerPane, scrollPane);
        rootLayout.setSpacing((w + h) / 40);
        centerPane.setAlignment(Pos.CENTER);
        leftPane.setAlignment(Pos.CENTER_LEFT);
        leftPane.setStyle("-fx-padding: 20;");
        rightPane.setAlignment(Pos.TOP_LEFT);
        rightPane.setStyle("-fx-padding: 20");
        topRow.setAlignment(Pos.CENTER);
        bottomRow.setAlignment(Pos.CENTER);

        scene = new Scene(rootLayout, w, h);
        stage.setScene(scene);

        // TODO: Button listeners preferably more atomic
        newGameBtn.setOnAction((e) -> {
            gameBoard.onStop();
            this.stage.setScene(menuScene);
        });

        resetBtn.setOnAction((e) -> {
            gameBoard.onStop();
            new Game(cfg, Menu.MENU_SCENE_WIDTH, Menu.MENU_SCENE_HEIGHT, menuScene, this.stage);
        });

        // reverts to the saved state of the board
        undoBtn.setOnAction((e) -> {
            if (lastGamestate != null && lastGamestate.board != gameBoard.representation()) {
                GAME_STOPPED = GAME_PAUSED = gameBoard.GAME_PAUSED = gameBoard.GAME_STOPPED = false;
                gameState.setText("");
                gameBoard.setGamestate(lastGamestate);
                movesLeftB.setText("Moves Left (Black): " + Integer.toString(lastGamestate.movesLeftB));
                movesLeftW.setText("Moves Left (White): " + Integer.toString(lastGamestate.movesLeftW));
                turn--;
                if (gameBoard.currentPlayer().piece == Board.WHITE) {
                    currentPlayer.setText("Turn: White");
                    currentPlayerHistory.setText("White");
                    totalWhiteTime.pop();
                } else {
                    currentPlayer.setText("Turn: Black");
                    currentPlayerHistory.setText("Black");
                    totalBlackTime.pop();
                }
                history.setText(history.getText() + currentPlayerHistory.getText() + " has undone their last move!" + "\n");
            }
        });

        stopBtn.setOnAction(e -> {
            gameBoard.onStop();
            gameBoard.GAME_STOPPED = true;
            gameBoard.GAME_PAUSED = gameBoard.GAME_STOPPED;
            GAME_STOPPED = gameBoard.GAME_STOPPED;
            GAME_PAUSED = gameBoard.GAME_PAUSED;
            gameState.setText(String.format("%s\n%s %3.2fs\n%s %3.2fs", "Game Stopped", "Total Black Time:", ((double)getTotalTime(totalBlackTime) / 1000)
                    , "Total White Time:", ((double)getTotalTime(totalWhiteTime) / 1000)));
        });
        pauseBtn.setOnAction(e -> {
            if (!gameBoard.GAME_STOPPED) {
                gameBoard.GAME_PAUSED = !gameBoard.GAME_PAUSED;
                GAME_PAUSED = gameBoard.GAME_PAUSED;
                if (gameBoard.GAME_PAUSED)
                    gameState.setText("Game Paused");
                else
                    gameState.setText("");
            }
        });

        gameBoard.doFirstRandMove();
    }

    private int getTotalTime(Stack<Integer> stack) {
        int total = 0;
        for (int x : stack) {
            total += x;
        }
        return total;
    }
}
