public class Player {
    public final byte piece;
    private int score = 0;

    Player(byte piece) {
        this.piece = piece;
    }

    public int score() { return score; }
    public int increaseScore() { return ++score; }
}
