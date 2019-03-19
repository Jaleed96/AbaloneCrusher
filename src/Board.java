import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class Board {
	public static final byte EMPTY = 'E', WHITE = 'W', BLACK = 'B';
	public static final int MAX_SIZE = 9; // vertically and horizontally

	private byte[][] board;
	private Cell[][] cells;
	private Pane pane;
	private Player current;
	private Player opponent;
	private List<Cell> selectedCells = new ArrayList<Cell>();

	Board(byte[][] board, double height) {
		this.board = board;
		pane = new Pane();

		current = new Player(Board.BLACK);
		opponent = new Player(Board.WHITE);

		double width = height / Math.sin(Math.PI / 3);
		Polygon background = Hexagon.drawable(width / 2, height / 2, width / 2, 0);
		background.setFill(Color.DEEPSKYBLUE);
		pane.getChildren().add(background);

		double yPadding = height / (MAX_SIZE + 5); // somewhat relative to cell height but mostly arbitrary
		double innerBoardHeight = height - yPadding * 2;
		double cellHeight = innerBoardHeight / 7; // Counting vertically: cell height * 5 + hex side * 4, where cell
		// height = 2 * hex side
		double xPadding = (width - Hexagon.width30Deg(cellHeight / 2) * 9) / 2;

		initCells(board, cellHeight, xPadding, yPadding);
		initSelectListeners(cells);
		setupBoard(board);
	}

	private void initCells(byte[][] board, double cellHeight, double xOffset, double yOffset) {
		cells = new Cell[board.length][];
		for (int row = 0; row < board.length; ++row) {
			cells[row] = new Cell[board[row].length];
		}

		double cellWidth = Hexagon.width30Deg(cellHeight / 2);
		double verticalHexOffset = Hexagon.verticalOffset30Deg(cellHeight / 2);
		double xWidthOffset = -(cellHeight - cellWidth) / 2;

		for (int row = 0; row < cells.length; ++row) {
			for (int col = 0; col < cells[row].length; ++col) {
				double x = cellWidth * (col + (MAX_SIZE - cells[row].length) / 2.0) + xWidthOffset + xOffset;
				double y = row * (cellHeight - verticalHexOffset) + yOffset;
				Cell c = new Cell(cellHeight, x, y, col, row);
				cells[row][col] = c;
				pane.getChildren().add(c);
			}
		}
	}

	private void initSelectListeners(Cell[][] cells) {
		for (Cell[] row : cells) {
			for (Cell c : row) {
				c.addEventHandler(MouseEvent.MOUSE_PRESSED, new EventHandler<MouseEvent>() {
					public void handle(MouseEvent me) {
						if (c.marble() != null && c.marble().playerCode() == current.piece) {
							handleMarbleClick(c);
						} else {
							try {
								handleEmptyClick(c);
							} catch (Exception e) {
								System.out.println("Illegal move.");
								dehighlightAllMarbles();
							}
						}
					}
				});
			}
		}
	}

	private void dehighlightAllMarbles() {
		for (Cell[] row : cells) {
			for (Cell cell : row) {
				if (cell.marble() != null) {
					cell.marble().dehighlightMarble();
				}
			}
		}
		selectedCells.clear();
	}

	private void handleMarbleClick(Cell c) {
		if (selectedCells.contains(c)) { //Already selected, deselect
			selectedCells.remove(selectedCells.indexOf(c));
			c.marble().dehighlightMarble();
		} else if (selectedCells.isEmpty()) { //No marbles selected, select one marble
			selectedCells.add(c);
			c.marble().highlightMarble();
		} else if (selectedCells.size() == 1) { //One marble selected, select if neighbour
			Coordinate existing = selectedCells.get(0).getCoordinate();
			if (BoardUtil.areNeighbors(existing, c.getCoordinate())) {
				selectedCells.add(c);
				c.marble().highlightMarble();
			} else { //One marble selected, select if new marble creates a valid group of 3
				Coordinate between = BoardUtil.findCoordBetween(existing, c.getCoordinate());
				if (between != null && cells[between.y][between.x].marble() != null) {
					cells[between.y][between.x].marble().highlightMarble();
					selectedCells.add(cells[between.y][between.x]);
					selectedCells.add(c);
					c.marble().highlightMarble();
				}
			}
		} else if (selectedCells.size() == 2) { //Two marbles selected, select if new marble creates valid group of 3
			Coordinate existingOne = selectedCells.get(0).getCoordinate();
			Coordinate existingTwo = selectedCells.get(1).getCoordinate();
			Coordinate betweenOne = BoardUtil.findCoordBetween(existingOne, c.getCoordinate());
			Coordinate betweenTwo = BoardUtil.findCoordBetween(existingTwo, c.getCoordinate());
			if ((betweenTwo != null && betweenTwo.equals(existingOne))
					|| (betweenOne != null && betweenOne.equals(existingTwo))) {
				selectedCells.add(c);
				c.marble().highlightMarble();
			}
		}
	}

	class sortByY implements Comparator<Cell> {
		// Used for sorting in ascending order of
		// roll number
		public int compare(Cell a, Cell b) {
			return a.getCoordinate().y - b.getCoordinate().y;
		}
	}

	class sortByX implements Comparator<Cell> {
		// Used for sorting in ascending order of
		// roll number
		public int compare(Cell a, Cell b) {
			return a.getCoordinate().x - b.getCoordinate().x;
		}
	}

	private void handleEmptyClick(Cell c) throws Exception {
		if (selectedCells.size() == 1) {
			moveOneMarble(c);
		} else {
			Coordinate cCoord = c.getCoordinate();
			Coordinate firstMarbleCoord = selectedCells.get(0).getCoordinate();
			Coordinate secondMarbleCoord = selectedCells.get(1).getCoordinate();
			boolean horizontalInline = cCoord.y == firstMarbleCoord.y;
			boolean verticalInline = firstMarbleCoord.y != secondMarbleCoord.y;
			if (horizontalInline || verticalInline) {
				moveInline(c, horizontalInline);
			} else {
				moveBroadside(c);
			}
		}
		dehighlightAllMarbles();
	}

	private void moveOneMarble(Cell c) throws Exception {
		Coordinate marble = selectedCells.get(0).getCoordinate();
		makeMove(new Move(new Push(marble, BoardUtil.neighborsOf(marble).fromCoordinate(c.getCoordinate()))));
	}

	private void moveInline(Cell c, boolean horizontalInline) throws Exception {
		Comparator<Cell> compare = horizontalInline ? new sortByX() : new sortByY();
		Collections.sort(selectedCells, compare);
		Coordinate marble = selectedCells.get(0).getCoordinate();
		boolean cIsSmaller = horizontalInline ? c.getCoordinate().x < marble.x : c.getCoordinate().y < marble.y;
		if (cIsSmaller) {
			Collections.swap(selectedCells, 0, selectedCells.size() - 1);
			marble = selectedCells.get(0).getCoordinate();
		}
		Coordinate lastMarble = selectedCells.get(selectedCells.size() - 1).getCoordinate();
		BoardUtil.Neighbor nextMarble = BoardUtil.neighborsOf(marble)
				.fromCoordinate(selectedCells.get(1).getCoordinate());
		if (BoardUtil.areNeighbors(lastMarble, c.getCoordinate())) {
			makeMove(new Move(new Push(marble, nextMarble)));
		}
	}

	private void moveBroadside(Cell c) throws Exception {
		Collections.sort(selectedCells, new sortByX());
		Coordinate rightMarble = selectedCells.get(0).getCoordinate();
		BoardUtil.Neighbor toFirstNeighbor = BoardUtil.neighborsOf(rightMarble).fromCoordinate(c.getCoordinate());
		BoardUtil.Direction moveDirection = toFirstNeighbor.direction;
		Coordinate secondMarble = selectedCells.get(1).getCoordinate();
		BoardUtil.Neighbor toSecondNeighbor = BoardUtil.neighborsOf(secondMarble).fromDirection(moveDirection);
		if (selectedCells.size() == 2) {
			makeMove(new Move(new Push(rightMarble, toFirstNeighbor), new Push(secondMarble, toSecondNeighbor)));
			dehighlightAllMarbles();
		} else {
			Coordinate thirdMarble = selectedCells.get(2).getCoordinate();
			BoardUtil.Neighbor toThirdNeighbor = BoardUtil.neighborsOf(thirdMarble).fromDirection(moveDirection);
			makeMove(new Move(new Push(rightMarble, toFirstNeighbor), new Push(secondMarble, toSecondNeighbor),
					new Push(thirdMarble, toThirdNeighbor)));
		}
	}

	public List<Move> inlineLegalMoves(Coordinate from) {
		List<Move> moves = new ArrayList<>();
		BoardUtil.Neighbors neighbors = BoardUtil.neighborsOf(from);
		for (BoardUtil.Neighbor to : neighbors.toArray()) {
			Move move = new Move(new Push(from, to));
			if (move.isLegalInline(this)) {
				moves.add(move);
			}
		}
		return moves;
	}

	private void setupBoard(byte[][] board) {
		for (int row = 0; row < board.length; ++row) {
			for (int col = 0; col < board[row].length; ++col) {
				Cell c = boardCell(col, row);
				if (board[row][col] == Board.EMPTY)
					c.setEmpty();
				else
					c.setMarble(new Marble(board[row][col]));
			}
		}
	}

	private Cell boardCell(int x, int y) {
		return cells[y][x];
	}

	public void makeMove(Move move) throws Move.IllegalMoveException {
		if (!move.isLegalInline(this) && !move.isLegalSideStep(this)) {
			StringBuilder erroMsg = new StringBuilder().append("Illegal move:");
			for (Push m : move.pushes()) {
				erroMsg.append(String.format(" [%s to %s]", m.from.toString(), m.to.coordinate.toString()));
			}
			throw new Move.IllegalMoveException(erroMsg.toString());
		}

		applyMove(move);
		nextTurn();
	}

	private void applyMove(Move move) {
		Optional<Byte> maybePushedOff = Optional.empty();
		Marble pushedOff = null;
		for (Push p : move.pushes()) {
			maybePushedOff = pushPiece(p);
			pushedOff = visualPushPiece(p);
		}

		final Marble finalPushedOff = pushedOff; // make compiler happy
		maybePushedOff.ifPresent(pushedOffPiece -> {
			// if the logic is right, pushedOff can never be null here;
			assert finalPushedOff != null;
			updateScore(pushedOffPiece);
		});
	}

	/// Pushes the piece in the board representation only, to update gui use
	/// visualPushPiece after this
	/// assumes that the move has been validated beforehand
	private Optional<Byte> pushPiece(Push p) {
		BoardUtil.Neighbor next = p.to;
		byte currentPiece = board[p.from.y][p.from.x];
		board[p.from.y][p.from.x] = Board.EMPTY;
		while (next != null && currentPiece != Board.EMPTY) {
			byte nextPiece = board[next.coordinate.y][next.coordinate.x];
			board[next.coordinate.y][next.coordinate.x] = currentPiece;
			next = next.neighbors().fromDirection(next.direction);
			currentPiece = nextPiece;
		}
		/// If the piece has been pushed off the board, return it
		return next == null ? Optional.of(currentPiece) : Optional.empty();
	}

	// this function is meant to be used right after pushPiece to update gui and
	// retrieve the pushedOff marble if there's one
	private Marble visualPushPiece(Push p) {
		BoardUtil.Neighbor next = p.to;
		Marble currentMarble = boardCell(p.from.x, p.from.y).removeMarble();
		while (next != null && currentMarble != null) {
			Marble nextMarble = boardCell(next.coordinate.x, next.coordinate.y).removeMarble();
			boardCell(next.coordinate.x, next.coordinate.y).setMarble(currentMarble);
			next = next.neighbors().fromDirection(next.direction);
			currentMarble = nextMarble;
		}
		return next == null ? currentMarble : null;
	}

	private void updateScore(byte pushedOffPiece) {
		if (currentOpponent().piece == pushedOffPiece)
			currentPlayer().increaseScore();
		else
			currentOpponent().increaseScore();
	}

	private void nextTurn() {
		Player t = current;
		current = opponent;
		opponent = t;
	}

	public Node drawable() {
		return pane;
	}

	public byte[][] representation() {
		return board;
	}

	public Player currentPlayer() {
		return current;
	}

	public Player currentOpponent() {
		return opponent;
	}
}
