public class Push {
    public final Coordinate from;
    public final BoardUtil.Neighbor to;

    Push(Coordinate from, BoardUtil.Neighbor to) {
        this.from = from;
        this.to = to;
    }
}
