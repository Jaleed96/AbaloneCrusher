import javafx.application.Application;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;

public class Menu extends Application {
    public static final int MENU_SCENE_WIDTH = 1300;
    public static final int MENU_SCENE_HEIGHT = 690;

    private GridPane gridpane = new GridPane();
    private Label title = new Label("ABALONE");
    private Label error = new Label("");
    private Label blackLabel = new Label("Black (P1): ");
    private Label whiteLabel = new Label("White (P2): ");
    private Label initialLayout = new Label("Initial Layout:");
    private Label p1timelimit = new Label("P1 Time Limit:");
    private Label p2timelimit = new Label("P2 Time Limit:");
    private Label moveLimit = new Label("Move limit per game: ");
    private TextField tbp1timelimit = new TextField("");
    private TextField tbp2timelimit = new TextField("");
    private TextField tbMoveLimit = new TextField("");
    private RadioButton humanRb = new RadioButton("Human");
    private RadioButton aiRb = new RadioButton("AI");
    private RadioButton standardRb = new RadioButton("Standard");
    private RadioButton germanRb = new RadioButton("German Daisy");
    private RadioButton belgianRb = new RadioButton("Belgian Daisy");
    private RadioButton humanRb2 = new RadioButton("Human");
    private RadioButton aiRb2 = new RadioButton("AI");
    private Button startBtn = new Button("START");
    private final ToggleGroup player1SelectionGroup = new ToggleGroup();
    private final ToggleGroup player2SelectionGroup = new ToggleGroup();
    private final ToggleGroup gameModeGroup = new ToggleGroup();

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Abalone");

        gridpane.setVgap(8);
        gridpane.setPadding(new Insets(0, 0, 0, 0));
        gridpane.setHgap(8);

        title.setFont(new Font(30));
        title.setStyle("-fx-font-weight: bold");

        initialLayout.setFont(new Font(15));
        initialLayout.setStyle("-fx-font-weight: bold");
        GridPane.setHalignment(initialLayout, HPos.CENTER);

        // set default values
        tbMoveLimit.setText("150");
        tbp1timelimit.setText("60");

        humanRb.setToggleGroup(player1SelectionGroup);
        aiRb.setToggleGroup(player1SelectionGroup);
        player1SelectionGroup.selectToggle(humanRb);
        humanRb2.setToggleGroup(player2SelectionGroup);
        aiRb2.setToggleGroup(player2SelectionGroup);
        player2SelectionGroup.selectToggle(humanRb2);
        standardRb.setToggleGroup(gameModeGroup);
        standardRb.setUserData(Config.InitialBoard.Standard);
        standardRb.setSelected(true);
        germanRb.setToggleGroup(gameModeGroup);
        germanRb.setUserData(Config.InitialBoard.GermanDaisy);
        belgianRb.setToggleGroup(gameModeGroup);
        belgianRb.setUserData(Config.InitialBoard.BelgianDaisy);
        GridPane.setConstraints(title, 25, 3);
        GridPane.setConstraints(blackLabel, 24, 10);
        GridPane.setConstraints(whiteLabel, 25, 10);
        GridPane.setConstraints(humanRb, 25, 10);
        GridPane.setConstraints(aiRb, 25, 11);
        GridPane.setConstraints(humanRb2, 26, 10);
        GridPane.setConstraints(aiRb2, 26, 11);
        GridPane.setConstraints(initialLayout, 25, 18);
        GridPane.setConstraints(standardRb, 24, 22);
        GridPane.setConstraints(germanRb, 25, 22);
        GridPane.setConstraints(belgianRb, 26, 22);
        GridPane.setConstraints(p1timelimit, 23, 13);
        GridPane.setConstraints(p2timelimit, 25, 13);
        GridPane.setConstraints(moveLimit, 24, 26);
        GridPane.setConstraints(tbp1timelimit, 24, 13);
        GridPane.setConstraints(tbp2timelimit, 26, 13);
        GridPane.setConstraints(tbMoveLimit, 25, 26);
        GridPane.setConstraints(error, 25, 30);

        GridPane.setConstraints(startBtn, 25, 29);
        GridPane.setHalignment(title, HPos.RIGHT);
        GridPane.setHalignment(blackLabel, HPos.RIGHT);
        GridPane.setHalignment(whiteLabel, HPos.RIGHT);

        double boardDisplayHeight = 191;
        Board standard = BoardUtil.makeStandardLayout(boardDisplayHeight);
        GridPane.setConstraints(standard.drawable(), 24, 21);

        Board german = BoardUtil.makeGermanDaisy(boardDisplayHeight);
        GridPane.setConstraints(german.drawable(), 25, 21);

        Board belgian = BoardUtil.makeBelgianDaisy(boardDisplayHeight);
        GridPane.setConstraints(belgian.drawable(), 26, 21);

        GridPane.setHalignment(standard.drawable(), HPos.CENTER);
        GridPane.setHalignment(german.drawable(), HPos.CENTER);
        GridPane.setHalignment(belgian.drawable(), HPos.CENTER);

        GridPane.setHalignment(standardRb, HPos.CENTER);
        GridPane.setHalignment(germanRb, HPos.CENTER);
        GridPane.setHalignment(belgianRb, HPos.CENTER);
        
        GridPane.setHalignment(tbp1timelimit, HPos.LEFT);
        GridPane.setHalignment(tbp2timelimit, HPos.LEFT);
        
        GridPane.setHalignment(p1timelimit, HPos.RIGHT);
        GridPane.setHalignment(p2timelimit, HPos.RIGHT);
      

        GridPane.setHalignment(startBtn, HPos.CENTER);


        gridpane.getChildren().addAll(title, standard.drawable(), german.drawable(), belgian.drawable(), blackLabel, whiteLabel, humanRb, aiRb, initialLayout, humanRb2,
                aiRb2, standardRb, germanRb, belgianRb, p1timelimit, p2timelimit, moveLimit, tbp1timelimit, tbp2timelimit, tbMoveLimit, startBtn, error);

        Scene scene = new Scene(gridpane, MENU_SCENE_WIDTH, MENU_SCENE_HEIGHT);

        startBtn.setOnAction(e -> {
            Config cfg = new Config();
            String errorMsg = "";
            String p1timelimitTxt = tbp1timelimit.getText().trim();
            if (!p1timelimitTxt.matches("[0-9]+")) {
                errorMsg += "Invalid input for time limit.";
            } else {
                cfg.p1timeLimit = Integer.parseInt(p1timelimitTxt);
            }

            String moveLimitTxt = tbMoveLimit.getText().trim();
            if (!moveLimitTxt.matches("[0-9]+")) {
                if (!errorMsg.isEmpty()) errorMsg += "\n";
                errorMsg += "Invalid input for move limit.";
            } else {
                cfg.moveLimit = Integer.parseInt(moveLimitTxt);
            }

            if (!errorMsg.isEmpty()) {
                error.setText(errorMsg);
                error.setTextFill(Color.RED);
                return;
            }

            cfg.initialLayout = (Config.InitialBoard) gameModeGroup.getSelectedToggle().getUserData();
            cfg.B_type = (Config.PlayerType) player1SelectionGroup.getSelectedToggle().getUserData();
            cfg.W_type = (Config.PlayerType) player2SelectionGroup.getSelectedToggle().getUserData();

            Game game = new Game(cfg, MENU_SCENE_WIDTH, MENU_SCENE_HEIGHT, scene, primaryStage);
        });


        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     * Launches the JavaFX application.
     *
     * @param args
     *            command line args.
     */
    public static void main(String[] args) {
        launch(args);
    }
}
