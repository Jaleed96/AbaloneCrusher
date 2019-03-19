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
        this.ellipse.setStrokeType(StrokeType.INSIDE);
        this.ellipse.setStrokeWidth(4);
        this.ellipse.setStroke(Color.ORANGE);
    }
    
    public void dehighlightMarble() {
        this.ellipse.setStroke(null);
    }

    public byte playerCode() { return playerCode; }
}
