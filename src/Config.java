public class Config {
    public enum PlayerType {
        AI, Human
    }

    public PlayerType B_type;
    public PlayerType W_type;

    public int B_timeLimit;
    public int W_timeLimit;
    public int moveLimit;

    Board initialLayout;
}
