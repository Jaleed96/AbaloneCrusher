import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class Game {
    private Scene scene;
    private Button newGameBtn, resetBtn, stopBtn, undoBtn, pauseBtn;
    private Label timeLabel;
    private TextField moveInput;

    Game(Config cfg, double w, double h) {
        BorderPane rootLayout = new BorderPane();
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
        undoBtn = new Button("Undo last move");
        bottomRow.getChildren().addAll(moveInput, undoBtn);

        centerPane.getChildren().addAll(topRow, bottomRow);

        // VBOX History box on the right
        VBox rightPane = new VBox(50);

        rootLayout.setLeft(leftPane);
        rootLayout.setCenter(centerPane);
        rootLayout.setRight(rightPane);



        scene = new Scene(rootLayout, w, h);
    }

    public Scene getScene() {
        return this.scene;
    }
}
