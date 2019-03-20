import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.StrokeType;

public class Marble extends Group {
    private Ellipse ellipse;
    private byte playerCode;
    private boolean selected;

    Marble(byte colorCode) {
        playerCode = colorCode;
        selected = false;
        ellipse = new Ellipse();
        if (colorCode == Board.BLACK) {
            ellipse.setFill(Color.BLACK);
        } else if (colorCode == Board.WHITE) {
            ellipse.setFill(Color.IVORY);
        }
        setDefaultStroke();
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
    
    public void highlightMarble() {
        ellipse.setStrokeType(StrokeType.INSIDE);
        ellipse.setStrokeWidth(4);
        ellipse.setStroke(Color.ORANGE);
    }

    private void setDefaultStroke() {
        ellipse.setStrokeType(StrokeType.INSIDE);
        ellipse.setStrokeWidth(.6);
        ellipse.setStroke(Color.BLACK);
    }
    
    public void dehighlightMarble() {
        setDefaultStroke();
    }

    public byte playerCode() { return playerCode; }
}
