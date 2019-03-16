import javafx.application.Application;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Font;
import javafx.stage.Stage;

public class Menu extends Application {
    
    GridPane gridpane = new GridPane();
    Label title = new Label("ABALONE");
    Label blackLabel = new Label("Black (P1): ");
    Label whiteLabel = new Label("White (P2): ");
    Label initialLayout = new Label("Initial Layout:");
    Label timeLimit = new Label("Time Limit per move (P1): ");
    Label timeLimit2 = new Label("Time Limit per move (P2): ");
    Label moveLimit = new Label("Move limit per game: ");
    TextField p1TimeLimit = new TextField("");
    TextField p2TimeLimit = new TextField("");
    TextField tbmoveLimit = new TextField("");
    RadioButton humanRb = new RadioButton("Human");
    RadioButton aiRb = new RadioButton("AI");
    RadioButton standardRb = new RadioButton("Standard");
    RadioButton germanRb = new RadioButton("German Daisy");
    RadioButton belgianRb = new RadioButton("Belgian Daisy");
    RadioButton humanRb2 = new RadioButton("Human");
    RadioButton aiRb2 = new RadioButton("AI");
    Button startBtn = new Button("START");
    final ToggleGroup player1SelectionGroup = new ToggleGroup();
    final ToggleGroup player2SelectionGroup = new ToggleGroup();
    final ToggleGroup gameModeGroup = new ToggleGroup();

    @Override
    public void start(Stage primaryStage) throws Exception {
        final int appHeight = 750;
        final int appWidth = 1100;
        primaryStage.setTitle("Abalone");
       
        gridpane.setVgap(8);
        gridpane.setPadding(new Insets(0, 0, 0,0));
        gridpane.setHgap(3);

        title.setFont(new Font(30));
        title.setStyle("-fx-font-weight: bold");
        
        initialLayout.setFont(new Font(15));
        initialLayout.setStyle("-fx-font-weight: bold");
        GridPane.setHalignment(initialLayout, HPos.CENTER);

        
        humanRb.setToggleGroup(player1SelectionGroup);
        aiRb.setToggleGroup(player1SelectionGroup);
        humanRb2.setToggleGroup(player2SelectionGroup);
        aiRb2.setToggleGroup(player2SelectionGroup);
        standardRb.setToggleGroup(gameModeGroup);
        standardRb.setUserData(Config.InitialBoard.Standard);
        standardRb.setSelected(true);
        germanRb.setToggleGroup(gameModeGroup);
        germanRb.setUserData(Config.InitialBoard.GermanDaisy);
        belgianRb.setToggleGroup(gameModeGroup);
        belgianRb.setUserData(Config.InitialBoard.BelgianDaisy);

        GridPane.setConstraints(title, 25, 3);
        GridPane.setConstraints(blackLabel, 23, 10);
        GridPane.setConstraints(whiteLabel, 25, 10);
        GridPane.setConstraints(humanRb, 24, 10);
        GridPane.setConstraints(aiRb, 24, 11);
        GridPane.setConstraints(humanRb2, 26, 10);
        GridPane.setConstraints(aiRb2, 26, 11);
        GridPane.setConstraints(initialLayout, 25, 18);
        GridPane.setConstraints(standardRb, 24, 22);
        GridPane.setConstraints(germanRb, 25, 22);
        GridPane.setConstraints(belgianRb, 26, 22);
        GridPane.setConstraints(timeLimit, 23, 14);
        GridPane.setConstraints(timeLimit2, 25, 14);
        GridPane.setConstraints(moveLimit, 24, 26);
        GridPane.setConstraints(p1TimeLimit, 24, 14);
        GridPane.setConstraints(tbmoveLimit, 25, 26);
        GridPane.setConstraints(p2TimeLimit, 26, 14);
        GridPane.setConstraints(startBtn, 25, 29);
        GridPane.setHalignment(blackLabel, HPos.RIGHT);
        GridPane.setHalignment(whiteLabel, HPos.RIGHT);
        GridPane.setHalignment(timeLimit2, HPos.RIGHT);
        
        ImageView standard = new ImageView(("/standard.png"));
        GridPane.setConstraints(standard, 24, 21);
        
        ImageView german = new ImageView(("/germandaisy.png"));
        GridPane.setConstraints(german, 25, 21);
        
        ImageView belgian = new ImageView(("/belgiandaisy.png"));
        GridPane.setConstraints(belgian, 26, 21);

        GridPane.setHalignment(standard, HPos.CENTER);
        GridPane.setHalignment(german, HPos.CENTER);
        GridPane.setHalignment(belgian, HPos.CENTER);
        
        GridPane.setHalignment(standardRb, HPos.CENTER);
        GridPane.setHalignment(germanRb, HPos.CENTER);
        GridPane.setHalignment(belgianRb, HPos.CENTER);
        
        GridPane.setHalignment(startBtn, HPos.CENTER);

        startBtn.setOnAction(e -> {
            Config cfg = new Config();
            cfg.initialLayout = (Config.InitialBoard) gameModeGroup.getSelectedToggle().getUserData();
            primaryStage.setScene(new Game(cfg, 1000, 750).getScene());
            primaryStage.show();
        });
         
        gridpane.getChildren().addAll(title, standard, german, belgian, blackLabel, whiteLabel, humanRb, aiRb, initialLayout, humanRb2, aiRb2, standardRb, germanRb, belgianRb, timeLimit, timeLimit2, moveLimit, p1TimeLimit, p2TimeLimit, tbmoveLimit, startBtn);

        Scene scene = new Scene(gridpane, appWidth, appHeight);
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
