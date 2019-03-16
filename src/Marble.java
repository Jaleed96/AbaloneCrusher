import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.Ellipse;

public class Marble extends Group {
    private Ellipse ellipse;
    private byte playerCode;

    Marble(byte colorCode) {
        playerCode = colorCode;
        ellipse = new Ellipse();
        if (colorCode == Board.BLACK) {
            ellipse.setFill(Color.BLACK);
        } else if (colorCode == Board.WHITE) {
            ellipse.setFill(Color.IVORY);
        }
        getChildren().add(ellipse);
    }

    public void setCenterPos(double x, double y) {
        ellipse.setCenterX(x);
        ellipse.setCenterY(y);
    }

    public void setRadius(double radius) {
        ellipse.setRadiusX(radius);
        ellipse.setRadiusY(radius);
    }

    public byte playerCode() { return playerCode; }
}
