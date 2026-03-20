package src.battleship.model;

import battleship.model.Coordinate;
import battleship.model.Orientation;
import battleship.model.Ship;
import battleship.model.ShipType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ShipTest {

    @Test
    void horizontalShipGeneratesCorrectCells() {
        Ship ship = new Ship(ShipType.FRIGATE, new Coordinate(3, 5), Orientation.HORIZONTAL);

        assertEquals(3, ship.getCells().size());
        assertEquals(3, ship.getCells().get(0).getRow());
        assertEquals(5, ship.getCells().get(0).getColumn());
        assertEquals(3, ship.getCells().get(2).getRow());
        assertEquals(7, ship.getCells().get(2).getColumn());
    }

    @Test
    void verticalShipGeneratesCorrectCells() {
        Ship ship = new Ship(ShipType.BOAT, new Coordinate(2, 4), Orientation.VERTICAL);

        assertEquals(2, ship.getCells().size());
        assertEquals(2, ship.getCells().get(0).getRow());
        assertEquals(4, ship.getCells().get(0).getColumn());
        assertEquals(3, ship.getCells().get(1).getRow());
        assertEquals(4, ship.getCells().get(1).getColumn());
    }

    @Test
    void registerImpactSinksShipAfterAllCellsHit() {
        Ship ship = new Ship(ShipType.BOAT, new Coordinate(0, 0), Orientation.HORIZONTAL);

        assertTrue(ship.registerImpact(new Coordinate(0, 0)));
        assertFalse(ship.isSunk());

        assertTrue(ship.registerImpact(new Coordinate(0, 1)));
        assertTrue(ship.isSunk());
    }

    @Test
    void registerImpactDoesNothingOnNonOccupiedCell() {
        Ship ship = new Ship(ShipType.BOAT, new Coordinate(0, 0), Orientation.HORIZONTAL);

        assertFalse(ship.registerImpact(new Coordinate(9, 9)));
        assertFalse(ship.isSunk());
    }
}