
public class Gamestate {
    
    private Player p1;
    private Player p2;
    
    private byte[][] board;
    private Player currentPlayer;
    private Player currentOpponent;
    
    public Gamestate(Player p1, Player p2, byte[][] board, Player currentPlayer, Player currentOpponent) {
        
        this.p1 = p1;
        this.p2 = p2;
        this.board = board;
        this.currentPlayer = currentPlayer;
        this.currentOpponent = currentOpponent;
    }
    
    
    


}
