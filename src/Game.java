import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.Background;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import org.omg.PortableInterceptor.SYSTEM_EXCEPTION;

import java.util.Timer;
import java.util.TimerTask;

public class Game {
    public boolean GAME_STOPPED = true;
    // USE GAME_PAUSED to check whether game is in session
    public boolean GAME_PAUSED = false;
    private Scene scene;
    private Stage stage;
    private Button newGameBtn, confirmBtn, resetBtn, stopBtn, undoBtn, pauseBtn;
    private Label blackScoreLabel;
    private Label blackScore;
    private Label whiteScoreLabel;
    private Label currentPlayer;
    private Label whiteScore;
    private Label timeLabel;
    private TextField moveInput;
    private Label gameState;
    private Label movesLeftW;
    private Label movesLeftB;
    private Timer timer;
    private int timeLimit;
    private int movesBlack;
    private int movesWhite;

    Game(Config cfg, double w, double h, Scene menuScene, Stage stage) {
        GAME_STOPPED = false;
        this.stage = stage;
        // BorderPane rootLayout = new BorderPane();
        HBox rootLayout = new HBox();
        rootLayout.setPrefSize(w, h);
        // VBOXButton pane on left
        VBox leftPane = new VBox(50);

        currentPlayer = new Label("Turn: Black");

        movesBlack = cfg.moveLimit;
        movesLeftB = new Label("Moves Left (Black): " + cfg.moveLimit);
        movesWhite = cfg.moveLimit;
        movesLeftW = new Label("Moves Left (White): " + cfg.moveLimit);

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
        leftPane.getChildren().addAll(movesLeftB, movesLeftW, currentPlayer, blackScoreRow, whiteScoreRow, newGameBtn, resetBtn, stopBtn);
        // VBOX Board, clock and input field in the center
        VBox centerPane = new VBox(50);
        HBox topRow = new HBox(50);

        // TODO: Export timing functionality into an inner class
        timeLabel = new Label(Integer.toString(cfg.timeLimit) + " s");
        int timeRes = 10;
        timeLimit  = cfg.timeLimit * 1000;
        timer = new Timer();
        timer.schedule(new TimerTask() {
            public void run() {
                Platform.runLater(new Runnable() {
                    public void run() {
                        if (timeLimit>=0 && !GAME_PAUSED) {
                            timeLabel.setText(String.format("%d.%03d s", timeLimit / 1000, timeLimit % 1000));
                            timeLimit -= timeRes;
                        }
                    }
                });
            }
        }, 0, timeRes);

        pauseBtn = new Button("Resume/Pause");
        gameState = new Label();
        topRow.getChildren().addAll(timeLabel, pauseBtn, gameState);

        HBox bottomRow = new HBox(50);
        moveInput = new TextField();
        confirmBtn = new Button("Confirm");

        undoBtn = new Button("Undo last move");
        bottomRow.getChildren().addAll(moveInput, confirmBtn, undoBtn);

        Board b = null;
        double boardHeight = 500;
        switch (cfg.initialLayout) {
        case Standard:
            b = BoardUtil.makeStandardLayout(boardHeight);
            break;
        case GermanDaisy:
            b = BoardUtil.makeGermanDaisy(boardHeight);
            break;
        case BelgianDaisy:
            b = BoardUtil.makeBelgianDaisy(boardHeight);
            break;
        }

        final Board finalB = b;

        finalB.setScoreUpdateListener((player, piece, gameOver) -> {
            if (player.piece == Board.WHITE) {
                whiteScore.setText(Integer.toString(player.score()));
            } else {
                blackScore.setText(Integer.toString(player.score()));
            }
        });

        finalB.setCurrentPlayerChangedListener(player -> {
            switch (Character.toString((char) player.piece)) {
            case "W":
                movesBlack--;
                movesLeftB.setText("Moves Left (Black): " + Integer.toString(movesBlack));
                currentPlayer.setText("Turn: White");
                timeLimit  = cfg.timeLimit * 1000;
                break;
            case "B":
                movesWhite--;
                movesLeftW.setText("Moves Left (White): " + Integer.toString(movesWhite));
                currentPlayer.setText("Turn: Black");
                timeLimit  = cfg.timeLimit * 1000;
                break;
            }
        });

        confirmBtn.setOnAction(e -> {
            if (!GAME_PAUSED) {
                Move move = null;
                try {
                    move = MoveParser.parse(moveInput.getText());
                } catch (Exception ex) {
                    // TODO display in a text field
                    System.out.println(ex.getMessage());
                    return;
                }

                try {
                    finalB.makeMove(move);
                } catch (Move.IllegalMoveException ex) {
                    // TODO display in a text field
                    System.out.println(ex.getMessage());
                }
            }
        });

        MoveSelection moveSelection = new MoveSelection(b);
        moveSelection.setOnMoveSelectedListener(move -> {
            if (!GAME_PAUSED) {
                try {
                    finalB.makeMove(move);
                } catch (Move.IllegalMoveException e) {
                    System.out.println(e.getMessage());
                }
            }
        });

        centerPane.getChildren().addAll(topRow, b.drawable(), bottomRow);

        // VBOX History box on the right
        VBox rightPane = new VBox(50);
        // TODO: Replace history box with formalized class that stores board history
        // These are dummy boxes
        Label move1 = new Label("1. A3 to B3 (B) - 3.435 s");
        Label move2 = new Label("2. C3-C5 to D4 (W) - 1.214 s");

        rightPane.getChildren().addAll(move1, move2);

        rootLayout.getChildren().addAll(leftPane, centerPane, rightPane);
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
            this.stage.setScene(menuScene);
        });

        resetBtn.setOnAction((e) -> {
           new Game(cfg, Menu.MENU_SCENE_WIDTH, Menu.MENU_SCENE_HEIGHT, menuScene, this.stage);
        });

        stopBtn.setOnAction(e -> {
            GAME_STOPPED = true;
            GAME_PAUSED = GAME_STOPPED;
            gameState.setText("Game Stopped");
        });
        pauseBtn.setOnAction(e -> {
            if (!GAME_STOPPED) {
                GAME_PAUSED = !GAME_PAUSED;
                if (GAME_PAUSED)
                    gameState.setText("Game Paused");
                else
                    gameState.setText("");
            }
        });
    }

    public Scene getScene() {
        return this.scene;
    }
}


