public class Config {
    public enum PlayerType {
        AI, Human
    }

    public enum InitialBoard {
        Standard, GermanDaisy, BelgianDaisy
    }

    public PlayerType B_type;
    public PlayerType W_type;

    public int timeLimit;
    public int moveLimit;

    InitialBoard initialLayout;
}
