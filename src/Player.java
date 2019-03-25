public class Player {
    public final byte piece;
    private int score = 0;
    private int timeLimit;
    private int moveLimit;
    

    Player(byte piece, int moveLimit, int timeLimit) {
        this.piece = piece;
        this.timeLimit = timeLimit;
        this.moveLimit = moveLimit;
    }

    public int score() { return score; }
    public int increaseScore() { return ++score; }
    public int getTimeLimit() { return timeLimit; }
    public int getMoveLimit() {return moveLimit;}
    
}
