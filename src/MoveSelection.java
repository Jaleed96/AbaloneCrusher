import javafx.scene.input.MouseEvent;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Collections;

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
        cells = context.cells();
        initSelectListeners(cells);
    }

    private void initSelectListeners(Cell[][] cells) {
        for (Cell[] row : cells) {
            for (Cell c : row) {
                c.addEventHandler(MouseEvent.MOUSE_PRESSED, me -> {
                    if (c.marble() != null && c.marble().playerCode() == context.currentPlayer().piece) {
                        handleMarbleClick(c);
                    } else {
                        Optional<Move> maybeMove = handleEmptyClick(c);
                        maybeMove.ifPresent(move -> selectionListener.moveSelected(move));
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

    private void handleMarbleClick(Cell c) {
        if (selectedCells.contains(c)) { // Already selected, deselect
            deselectCell(c);
        } else if (selectedCells.isEmpty()) { // No marbles selected, select one marble
            selectCell(c);
        } else if (selectedCells.size() == 1) { // One marble selected, select if neighbour
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

    private Optional<Move> handleEmptyClick(Cell c) {
        Optional<Move> maybeMove = Optional.empty();
        if (selectedCells.size() == 1) {
            maybeMove = moveOneMarble(c);
        } else if (!selectedCells.isEmpty()){
            Coordinate cCoord = c.getCoordinate();
            Coordinate firstMarbleCoord = selectedCells.get(0).getCoordinate();
            Coordinate secondMarbleCoord = selectedCells.get(1).getCoordinate();
            boolean horizontalInline = cCoord.y == firstMarbleCoord.y;
            boolean verticalInline = firstMarbleCoord.y != secondMarbleCoord.y;
            if (horizontalInline || verticalInline) {
                maybeMove = moveInline(c, horizontalInline);
            } else {
                maybeMove = moveBroadside(c);
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
        Comparator<Cell> compare = horizontalInline ? new sortByX() : new sortByY();
        selectedCells.sort(compare);
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
            return Optional.of(new Move(new Push(marble, nextMarble)));
        }
        return Optional.empty();
    }

    private Optional<Move> moveBroadside(Cell c) {
        selectedCells.sort(new sortByX());
        Coordinate rightMarble = selectedCells.get(0).getCoordinate();
        BoardUtil.Neighbor toFirstNeighbor = BoardUtil.neighborsOf(rightMarble).fromCoordinate(c.getCoordinate());
        if (toFirstNeighbor == null)
            return Optional.empty();
        BoardUtil.Direction moveDirection = toFirstNeighbor.direction;
        Coordinate secondMarble = selectedCells.get(1).getCoordinate();
        BoardUtil.Neighbor toSecondNeighbor = BoardUtil.neighborsOf(secondMarble).fromDirection(moveDirection);
        if (selectedCells.size() == 2) {
            return Optional.of(new Move(new Push(rightMarble, toFirstNeighbor),
                                        new Push(secondMarble, toSecondNeighbor)));
        } else {
            Coordinate thirdMarble = selectedCells.get(2).getCoordinate();
            BoardUtil.Neighbor toThirdNeighbor = BoardUtil.neighborsOf(thirdMarble).fromDirection(moveDirection);
            return Optional.of(new Move(new Push(rightMarble, toFirstNeighbor),
                                        new Push(secondMarble, toSecondNeighbor),
                                        new Push(thirdMarble, toThirdNeighbor)));
        }
    }

    public void setOnMoveSelectedListener(OnMoveSelectionListener listen) {
        this.selectionListener = listen;
    }
}
