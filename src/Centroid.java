public class Centroid {
    double x;
    double y;
    double w = 1.0;

    Centroid(double x, double y) {
        this.x = x;
        this.y = y;
    }

    void setWeight(double w) {
        this.w = w;
    }
}
