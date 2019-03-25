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
import javafx.scene.control.ScrollPane;

import java.util.Timer;

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
    private TextField moveInput;
    private Label gameState;
    private Label movesLeftW;
    private Label movesLeftB;
    private Label history;
    private Timer timer;
    private CheckBox toggleCoordOverlay;
    private int timeLimit;

    private int movesBlack;
    private int movesWhite;
    private Gamestate lastGamestate;
    Board b;

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
        timeLabel = new Label(Integer.toString(cfg.p1timeLimit));
        topRow.getChildren().addAll(timeLabel, pauseBtn, gameState);

        HBox bottomRow = new HBox(50);
        moveInput = new TextField();
        confirmBtn = new Button("Confirm");

        undoBtn = new Button("Undo last move");
        bottomRow.getChildren().addAll(moveInput, confirmBtn, undoBtn);

        b = null;
        double boardHeight = 500;
        switch (cfg.initialLayout) {
        case Standard:
            b = BoardUtil.makeStandardLayout(boardHeight, cfg.moveLimit, cfg.p1timeLimit, cfg.p2timeLimit);
            break;
        case GermanDaisy:
            b = BoardUtil.makeGermanDaisy(boardHeight, cfg.moveLimit, cfg.p1timeLimit, cfg.p2timeLimit);
            break;
        case BelgianDaisy:
            b = BoardUtil.makeBelgianDaisy(boardHeight, cfg.moveLimit, cfg.p1timeLimit, cfg.p2timeLimit);
            break;
        }
        
             
        final Board finalB = b;
        GAME_PAUSED = finalB.GAME_PAUSED;
        GAME_STOPPED = finalB.GAME_STOPPED;

        finalB.setTimeUpdatedListener((player, timeLeft) -> {
            timeLabel.setText(String.format("%d.%03d s", timeLeft / 1000, timeLeft % 1000));
        });

        movesLeftB = new Label("Moves Left (Black): " + finalB.blackMovesLeft);
        movesLeftW = new Label("Moves Left (White): " + finalB.whiteMovesLeft);
        leftPane.getChildren().addAll(movesLeftB, movesLeftW, currentPlayer, blackScoreRow, whiteScoreRow, newGameBtn, resetBtn, stopBtn, toggleCoordOverlay);

        finalB.setTextCoordVisibility(toggleCoordOverlay.isSelected());
        toggleCoordOverlay.selectedProperty().addListener((observable, wasChecked, isChecked) -> {
            finalB.setTextCoordVisibility(isChecked);
        });

        finalB.setScoreUpdateListener((player, piece, gameOver) -> {
            if (player.piece == Board.WHITE) {
                whiteScore.setText(Integer.toString(player.score()));
            } else {
                blackScore.setText(Integer.toString(player.score()));
            }
        });

        finalB.setCurrentPlayerChangedListener(player -> {
            switch (player.piece) {
            case 'W':
                movesLeftB.setText("Moves Left (Black): " + Integer.toString(finalB.blackMovesLeft));
                currentPlayer.setText("Turn: White");
                //timeLimit  = cfg.p1timeLimit * 1000;
                break;
            case 'B':
                movesLeftW.setText("Moves Left (White): " + Integer.toString(finalB.whiteMovesLeft));
                currentPlayer.setText("Turn: Black");
                //timeLimit  = cfg.p1timeLimit * 1000;
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
                    System.out.println("Saving state");
                    //saves the current state of board before a move is made
                    lastGamestate = new Gamestate(finalB.representation(), finalB.currentPlayer(), finalB.currentOpponent(), finalB.blackMovesLeft, finalB.whiteMovesLeft);
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
        
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setContent(rightPane);
        scrollPane.setPannable(true);
        rightPane.getChildren().addAll(move1, move2);

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
            this.stage.setScene(menuScene);
        });

        resetBtn.setOnAction((e) -> {
           new Game(cfg, Menu.MENU_SCENE_WIDTH, Menu.MENU_SCENE_HEIGHT, menuScene, this.stage);
        });
        
        //reverts to the saved state of the board
        undoBtn.setOnAction((e) -> {
            
            finalB.setBoard(lastGamestate.board);           
            finalB.setCurrent(lastGamestate.currentPlayer);
            finalB.setOpponent(lastGamestate.opponent);
            finalB.blackMovesLeft = lastGamestate.movesLeftB;
            finalB.whiteMovesLeft = lastGamestate.movesLeftW;
            movesLeftB.setText("Moves Left (Black): " + Integer.toString(lastGamestate.movesLeftB));
            movesLeftW.setText("Moves Left (White): " + Integer.toString(lastGamestate.movesLeftW));
            if (finalB.currentPlayer().piece == 'W') {
                currentPlayer.setText("Turn: White");
            } else {
                currentPlayer.setText("Turn: Black");
            }
            
                             
         });

        stopBtn.setOnAction(e -> {
            finalB.GAME_STOPPED = true;
            finalB.GAME_PAUSED = finalB.GAME_STOPPED;
            GAME_STOPPED = finalB.GAME_STOPPED;
            GAME_PAUSED = finalB.GAME_PAUSED;
            gameState.setText("Game Stopped");
        });
        pauseBtn.setOnAction(e -> {
            if (!finalB.GAME_STOPPED) {
                finalB.GAME_PAUSED = !finalB.GAME_PAUSED;
                GAME_PAUSED = finalB.GAME_PAUSED;
                if (finalB.GAME_PAUSED)
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


