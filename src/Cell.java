import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Shape;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

public class Cell extends StackPane {
    private static final Color BORDER_COLOR = Color.BLACK;
    private static final Color BACKGROUND_COLOR = Color.LIGHTSKYBLUE;
    private static final Color OFF_THE_BOARD = Color.GRAY;

    private static final Color TXT_COLOR_ON_EMPTY = Color.BLACK;
    private static final Color TXT_COLOR_ON_WHITE = Color.BLACK;
    private static final Color TXT_COLOR_ON_BLACK = Color.WHITE;

    private Shape shape;
    private Marble marble;
    private Text txtCoord;
    private double centerX;
    private double centerY;
    private double side;
    private Coordinate coord;

    // assuming top left coordinates
    public Cell(double size, double x, double y, Coordinate coord) {
        this.coord = coord;

        setTranslateX(x);
        setTranslateY(y);

        side = size / 2;
        centerX = x + side;
        centerY = y + side;

        shape = Hexagon.drawable30Deg(0, 0, side);
        shape.setStroke(BORDER_COLOR);
        shape.setStrokeWidth(1);
        shape.setFill(BACKGROUND_COLOR);

        txtCoord = new Text(BoardUtil.toConformanceCoord(coord));
        txtCoord.setFill(TXT_COLOR_ON_EMPTY);
        txtCoord.setFont(new Font(size / 5));
        txtCoord.setVisible(false);
        txtCoord.setMouseTransparent(true);

        getChildren().addAll(shape, txtCoord);
    }

    public void setMarble(Marble m) {
        setEmpty();
        marble = m;
        marble.setRadius(side - side / 2.5);
        getChildren().add(marble);
        if (marble.playerCode() == Board.WHITE) {
            txtCoord.setFill(TXT_COLOR_ON_WHITE);
        } else if (marble.playerCode() == Board.BLACK) {
            txtCoord.setFill(TXT_COLOR_ON_BLACK);
        }
        txtCoord.toFront();
    }

    public Marble removeMarble() {
        Marble m = marble;
        getChildren().remove(marble);
        marble = null;
        txtCoord.setFill(TXT_COLOR_ON_EMPTY);
        return m;
    }

    public void setTextCoordVisibility(boolean visible) {
        txtCoord.setVisible(visible);
    }

    public void setEmpty() {
        removeMarble();
    }

    public void markOffTheBoard() {
        shape.setFill(OFF_THE_BOARD);
    }

    public Marble marble() {
        return marble;
    }

    public Coordinate getCoordinate() {
    	return coord;
    }
}
