public class Config {
    public enum PlayerAgent {
        AI, Human
    }

    public enum InitialBoard {
        Standard, GermanDaisy, BelgianDaisy
    }

    public PlayerAgent blackAgent;
    public PlayerAgent whiteAgent;

    public int blackTimeLimitMs;
    public int whiteTimeLimitMs;
    public int moveLimit;

    InitialBoard initialLayout;
}
