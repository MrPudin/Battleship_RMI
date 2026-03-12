package battleship.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Ship implements Serializable {
    private final ShipType type;
    private final Coordinate start;
    private final Orientation orientation;
    private final List<Coordinate> impacts = new ArrayList<>();

    public Ship(ShipType type, Coordinate start, Orientation orientation) {
        this.type = type;
        this.start = start;
        this.orientation = orientation;
    }

    public ShipType getType() {
        return type;
    }

    public Coordinate getStart() {
        return start;
    }

    public Orientation getOrientation() {
        return orientation;
    }

    public List<Coordinate> getCells() {
        List<Coordinate> cells = new ArrayList<>();
        for (int i = 0; i < type.getSize(); i++) {
            int row = start.getRow() + (orientation == Orientation.VERTICAL ? i : 0);
            int column = start.getColumn() + (orientation == Orientation.HORIZONTAL ? i : 0);
            cells.add(new Coordinate(row, column));
        }
        return cells;
    }

    public boolean ocupa(Coordinate c) {
        return getCells().stream().anyMatch(x -> x.getRow() == c.getRow() && x.getColumn() == c.getColumn());
    }

    public boolean registerImpact(Coordinate c) {
        if (!ocupa(c)) return false;
        boolean yaImpactada = impacts.stream().anyMatch(x -> x.getRow() == c.getRow() && x.getColumn() == c.getColumn());
        if (!yaImpactada) impacts.add(c);
        return true;
    }

    public boolean itsSunk() {
        return impacts.size() == type.getSize();
    }
}
