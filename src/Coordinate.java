public class Coordinate {
    public final int x;
    public final int y;

    Coordinate(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public String toString() {
        return "(" + x + "," + y + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Coordinate))
            return false;

        Coordinate other = (Coordinate) o;
        return x == other.x && y == other.y;
    }
}
