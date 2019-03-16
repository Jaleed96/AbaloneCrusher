import javafx.scene.shape.Polygon;

public class Hexagon {
    public static Polygon drawable(double centerX, double centerY, double side, double rotation /* in rad */) {
        Polygon polygon = new Polygon();
        for (int i = 0; i < 6; i++){
            double angle_rad = i * Math.PI / 3 + rotation;
            polygon.getPoints().add(centerX + side * Math.cos(angle_rad));
            polygon.getPoints().add(centerY + side * Math.sin(angle_rad));
        }
        return polygon;
    }

    public static Polygon drawable30Deg(double centerX, double centerY, double side) {
        return drawable(centerX, centerY, side, Math.PI / 6);
    }

    public static double width30Deg(double side) {
        return 2 * side * Math.sin(Math.PI / 3);
    }

    // height of the angled side
    public static double verticalOffset30Deg(double side) {
        return side * Math.sin(Math.PI / 6);
    }
}
