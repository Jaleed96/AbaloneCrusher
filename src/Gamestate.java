import java.util.Arrays;

import org.omg.CORBA.Current;

import javafx.scene.control.Label;

public class Gamestate {
    

    Player currentPlayer;
    Player current;
    Player opponent;
    public byte[][] board;
    public Label currentTurn;
    int movesLeftW;
    int movesLeftB;
    
 
    public Gamestate(byte[][] board, Player currentPlayer, Player opponentPlayer, int movesLeftB, int movesLeftW) {
        
        this.board = BoardUtil.deepCopyRepresentation(board); 
        this.currentPlayer = new Player(currentPlayer.piece, currentPlayer.getMoveLimit(), currentPlayer.getTimeLimit());      
        this.opponent = new Player(opponentPlayer.piece, opponentPlayer.getMoveLimit(), opponentPlayer.getTimeLimit());      
        this.movesLeftB = movesLeftB;
        this.movesLeftW = movesLeftW;
       
        
    }


}
