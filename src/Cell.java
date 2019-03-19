import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.Shape;

public class Cell extends Group {
    private static final Color BORDER_COLOR = Color.BLACK;
    private static final Color BACKGROUND_COLOR = Color.LIGHTSKYBLUE;
    private static final Color OFF_THE_BOARD = Color.GRAY;

    private Shape shape;
    private Marble marble;
    private double centerX;
    private double centerY;
    private double side;
    private int coordX;
    private int coordY;

    // assuming top left coordinates
    public Cell(double size, double x, double y, int coordX, int coordY) {
        side = size / 2;
        centerX = x + side;
        centerY = y + side;
        
        this.coordX = coordX;
        this.coordY = coordY;

        shape = Hexagon.drawable30Deg(centerX, centerY, side);
        shape.setStroke(BORDER_COLOR);
        shape.setStrokeWidth(1);
        shape.setFill(BACKGROUND_COLOR);

        getChildren().addAll(shape);
    }

    public void setMarble(Marble m) {
        setEmpty();
        marble = m;
        marble.setCenterPos(centerX, centerY);
        marble.setRadius(side - side / 2.5);
        getChildren().add(marble);
    }

    public Marble removeMarble() {
        Marble m = marble;
        getChildren().remove(marble);
        marble = null;
        return m;
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
    	return BoardUtil.COORDINATES[coordY][coordX];
    }
}
