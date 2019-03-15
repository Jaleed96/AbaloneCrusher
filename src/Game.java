import javafx.scene.Scene;
import javafx.scene.layout.HBox;

public class Game {
    private Scene scene;

    Game(Config cfg, double w, double h) {
        HBox rootLayout = new HBox();
        scene = new Scene(rootLayout, w, h);
    }
}
