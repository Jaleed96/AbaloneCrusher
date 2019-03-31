public class Gamestate {
    Player currentPlayer;
    Player opponent;
    byte[][] board;
    int movesLeftW;
    int movesLeftB;

    Gamestate(byte[][] board, Player currentPlayer, Player opponentPlayer, int movesLeftB, int movesLeftW) {
        this.board = BoardUtil.deepCopyRepresentation(board);
        this.currentPlayer = new Player(currentPlayer);
        this.opponent = new Player(opponentPlayer);
        this.movesLeftB = movesLeftB;
        this.movesLeftW = movesLeftW;
    }

    Gamestate(Gamestate toCopy) {
        this.board = BoardUtil.deepCopyRepresentation(toCopy.board);
        this.currentPlayer = new Player(toCopy.currentPlayer);
        this.opponent = new Player(toCopy.opponent);
        this.movesLeftB = toCopy.movesLeftB;
        this.movesLeftW = toCopy.movesLeftW;
    }
}
