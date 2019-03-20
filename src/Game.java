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
import javafx.stage.Stage;

public class Game {
    public static boolean GAME_IN_SESSION = false;
    private Scene scene;
    private Stage stage;
    private Button newGameBtn, confirmBtn, resetBtn, stopBtn, undoBtn, pauseBtn;
    private Label timeLabel;
    private TextField moveInput;

    Game(Config cfg, double w, double h, Scene menuScene, Stage stage) {
        GAME_IN_SESSION = true;
        this.stage = stage;
        //BorderPane rootLayout = new BorderPane();
        HBox rootLayout = new HBox();
        rootLayout.setPrefSize(w, h);
        // VBOXButton pane on left
        VBox leftPane = new VBox(50);
        newGameBtn = new Button("New Game");
        resetBtn = new Button("Reset Game");
        stopBtn = new Button("Stop");
        leftPane.getChildren().addAll(newGameBtn,resetBtn, stopBtn);
        // VBOX Board, clock and input field in the center
        VBox centerPane = new VBox(50);
        HBox topRow = new HBox(50);
        timeLabel = new Label("20.000 ms");
        pauseBtn = new Button("Pause");
        topRow.getChildren().addAll(timeLabel, pauseBtn);

        HBox bottomRow = new HBox(50);
        moveInput = new TextField();
        confirmBtn = new Button("Confirm");



        undoBtn = new Button("Undo last move");
        bottomRow.getChildren().addAll(moveInput, confirmBtn, undoBtn);

        Board b = null;
        double boardHeight = 500;
        switch (cfg.initialLayout) {
            case Standard:     b = BoardUtil.makeStandardLayout(boardHeight); break;
            case GermanDaisy:  b = BoardUtil.makeGermanDaisy(boardHeight);    break;
            case BelgianDaisy: b = BoardUtil.makeBelgianDaisy(boardHeight);   break;
        }

        final Board finalB = b;

        if (GAME_IN_SESSION) {
            confirmBtn.setOnAction(e -> {
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
            });

            MoveSelection moveSelection = new MoveSelection(b);
            moveSelection.setOnMoveSelectedListener(move -> {
                try {
                    finalB.makeMove(move);
                } catch (Move.IllegalMoveException e) {
                    System.out.println(e.getMessage());
                }
            });
        }


        centerPane.getChildren().addAll(topRow, b.drawable(), bottomRow);

        // VBOX History box on the right
        VBox rightPane = new VBox(50);
        // TODO: Replace history box with formalized class that stores board history
        // These are dummy boxes
        Label move1 = new Label("1. H1-H2 (B)");
        Label move2 = new Label("2. I5-I2 (W)");

        rightPane.getChildren().addAll(move1, move2);

        rootLayout.getChildren().addAll(leftPane, centerPane, rightPane);
        rootLayout.setSpacing((w+h)/40);
        centerPane.setAlignment(Pos.CENTER);
        leftPane.setAlignment(Pos.CENTER_LEFT);
        leftPane.setStyle("-fx-padding: 20;");
        rightPane.setAlignment(Pos.TOP_LEFT);
        rightPane.setStyle("-fx-padding: 20");
        topRow.setAlignment(Pos.CENTER);
        bottomRow.setAlignment(Pos.CENTER);

        scene = new Scene(rootLayout, w, h);
        stage.setScene(scene);

        // TODO: Button listeners preferablly more atomic
        newGameBtn.setOnAction((e) -> {
            stage.setScene(menuScene);
        });
        stopBtn.setOnAction(e -> {
            GAME_IN_SESSION = false;
        });
    }

    public Scene getScene() {
        return this.scene;
    }
}
