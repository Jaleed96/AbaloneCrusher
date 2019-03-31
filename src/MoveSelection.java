import javafx.scene.input.MouseDragEvent;
import javafx.scene.input.MouseEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MoveSelection {

    public interface OnMoveSelectionListener {
        void moveSelected(Move move);
    }

    private Cell[][] cells;
    private List<Cell> selectedCells = new ArrayList<>();
    private List<Marble> highlightedMarbles = new ArrayList<>();
    private Board context;
    private OnMoveSelectionListener selectionListener = move -> {};

    MoveSelection(Board context) {
        this.context = context;
        cells = context.gui().cells();
        initSelectListeners(cells);
    }

    private void initSelectListeners(Cell[][] cells) {
        for (Cell[] row : cells) {
            for (Cell c : row) {
                c.addEventHandler(MouseEvent.MOUSE_CLICKED, me -> {
                    dehighlightAllMarbles();
                });
                c.addEventHandler(MouseEvent.MOUSE_PRESSED, me -> {
                    if (hasCurrentPlayersMarble(c))
                        handleMarbleSelect(c);
                });
                c.addEventHandler(MouseEvent.DRAG_DETECTED, me -> {
                    c.startFullDrag();
                });
                c.addEventHandler(MouseDragEvent.MOUSE_DRAG_ENTERED, me -> {
                    if (hasCurrentPlayersMarble(c)) {
                        handleMarbleSelect(c);
                    }
                });
                c.addEventHandler(MouseDragEvent.MOUSE_DRAG_RELEASED, me -> {
                    if (selectedCells.size() == 1 && selectedCells.get(0) == c) {
                        deselectCell(c);
                    } else if (c.marble() == null || c.marble().playerCode() == context.currentOpponent().piece) {
                        Optional<Move> maybeMove = handleDestinationSelect(c);
                        maybeMove.ifPresent(move -> selectionListener.moveSelected(move));
                    } else {
                        dehighlightAllMarbles();
                    }
                });
            }
        }
    }

    private void dehighlightAllMarbles() {
        highlightedMarbles.forEach(Marble::dehighlightMarble);
        highlightedMarbles.clear();
        selectedCells.clear();
    }

    private void handleMarbleSelect(Cell c) {
        if (selectedCells.isEmpty()) { // No marbles selected, select one marble
            selectCell(c);
        } else if (selectedCells.size() == 1 && selectedCells.get(0) != c) { // One marble selected, select if neighbour
            Coordinate existing = selectedCells.get(0).getCoordinate();
            if (BoardUtil.areNeighbors(existing, c.getCoordinate())) {
                selectCell(c);
            } else { // One marble selected, select if new marble creates a valid group of 3
                Coordinate between = BoardUtil.findCoordBetween(existing, c.getCoordinate());
                if (between != null && cells[between.y][between.x].marble() != null) {
                    selectCell(cells[between.y][between.x]);
                    selectCell(c);
                }
            }
        } else if (selectedCells.size() > 1 && selectedCells.subList(0, selectedCells.size() - 1).contains(c)) {
            int cIndex = selectedCells.indexOf(c);
            while (selectedCells.size() != cIndex + 1)
                deselectCell(selectedCells.get(selectedCells.size() - 1));
        } else if (selectedCells.size() == 2) { // Two marbles selected, select if new marble creates valid group of 3
            Coordinate existingOne = selectedCells.get(0).getCoordinate();
            Coordinate existingTwo = selectedCells.get(1).getCoordinate();
            Coordinate betweenOne = BoardUtil.findCoordBetween(existingOne, c.getCoordinate());
            Coordinate betweenTwo = BoardUtil.findCoordBetween(existingTwo, c.getCoordinate());
            if ((betweenTwo != null && betweenTwo.equals(existingOne))
                    || (betweenOne != null && betweenOne.equals(existingTwo))) {
                selectCell(c);
            }
        }
    }

    private void selectCell(Cell c) {
        selectedCells.add(c);
        c.marble().highlightMarble();
        highlightedMarbles.add(c.marble());
    }

    private void deselectCell(Cell c) {
        selectedCells.remove(c);
        c.marble().dehighlightMarble();
        highlightedMarbles.remove(c.marble());
    }

    private Optional<Move> handleDestinationSelect(Cell c) {
        Optional<Move> maybeMove = Optional.empty();
        if (selectedCells.size() == 1) {
            maybeMove = moveOneMarble(c);
        } else if (!selectedCells.isEmpty()) {
            Coordinate cCoord = c.getCoordinate();
            Coordinate frontMarbleCoord = selectedCells.get(selectedCells.size() - 1).getCoordinate();
            Coordinate nextMarbleCoord = selectedCells.get(selectedCells.size() - 2).getCoordinate();
            BoardUtil.Neighbor frontCellNeighbor = BoardUtil.neighborsOf(frontMarbleCoord).fromCoordinate(cCoord);
            if (frontCellNeighbor != null) {
                BoardUtil.Direction moveDirection = frontCellNeighbor.direction;
                BoardUtil.Neighbor marbleNeighbor = BoardUtil.neighborsOf(nextMarbleCoord)
                        .fromCoordinate(frontMarbleCoord);
                if (marbleNeighbor != null) {
                    BoardUtil.Direction marbleDirection = marbleNeighbor.direction;
                    boolean broadside = moveDirection != marbleDirection;
                    boolean horizontalInline = !broadside && cCoord.y == nextMarbleCoord.y;
                    maybeMove = broadside ? moveBroadside(c) : moveInline(c, horizontalInline);
                }
            }
        }
        dehighlightAllMarbles();
        return maybeMove;
    }

    private Optional<Move> moveOneMarble(Cell c) {
        Coordinate marble = selectedCells.get(0).getCoordinate();
        BoardUtil.Neighbor neighborTo = BoardUtil.neighborsOf(marble).fromCoordinate(c.getCoordinate());
        if (neighborTo == null)
            return Optional.empty();
        return Optional.of(new Move(new Push(marble, neighborTo)));
    }

    private Optional<Move> moveInline(Cell c, boolean horizontalInline) {
        Coordinate marble = selectedCells.get(0).getCoordinate();
        Coordinate lastMarble = selectedCells.get(selectedCells.size() - 1).getCoordinate();
        BoardUtil.Neighbor nextMarble = BoardUtil.neighborsOf(marble)
                .fromCoordinate(selectedCells.get(1).getCoordinate());
        if (BoardUtil.areNeighbors(lastMarble, c.getCoordinate())) {
            return Optional.of(new Move(new Push(marble, nextMarble)));
        }
        return Optional.empty();
    }

    private Optional<Move> moveBroadside(Cell c) {
        Coordinate firstMarble = selectedCells.get(selectedCells.size() - 1).getCoordinate();
        BoardUtil.Neighbor toFirstNeighbor = BoardUtil.neighborsOf(firstMarble).fromCoordinate(c.getCoordinate());
        if (toFirstNeighbor == null)
            return Optional.empty();
        BoardUtil.Direction moveDirection = toFirstNeighbor.direction;
        Coordinate lastMarble = selectedCells.get(0).getCoordinate();
        BoardUtil.Neighbor toLastNeighbor = BoardUtil.neighborsOf(lastMarble).fromDirection(moveDirection);
        if (selectedCells.size() == 2) {
            return Optional.of(new Move(new Push(firstMarble, toFirstNeighbor),
                                        new Push(lastMarble, toLastNeighbor)));
        } else {
            Coordinate middleMarble = selectedCells.get(1).getCoordinate();
            BoardUtil.Neighbor toMiddleNeighbor = BoardUtil.neighborsOf(middleMarble).fromDirection(moveDirection);
            return Optional.of(new Move(new Push(firstMarble, toFirstNeighbor),
                                        new Push(middleMarble, toMiddleNeighbor),
                                        new Push(lastMarble, toLastNeighbor)));
        }
    }

    private boolean hasCurrentPlayersMarble(Cell c) {
        return c.marble() != null && c.marble().playerCode() == context.currentPlayer().piece;
    }

    public void setOnMoveSelectedListener(OnMoveSelectionListener listen) {
        this.selectionListener = listen;
    }
}
