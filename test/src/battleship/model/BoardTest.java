package src.battleship.model;

import battleship.dto.ShotResolutionDTO;
import battleship.model.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class BoardTest {

    @Test
    void placeShipRejectsOutOfBounds() {
        Board board = new Board();
        Ship ship = new Ship(ShipType.AIRCRAFTCARRIER, new Coordinate(9, 9), Orientation.HORIZONTAL);

        assertFalse(board.placeShip(ship));
    }

    @Test
    void placeShipRejectsCollisionAndAdjacency() {
        Board board = new Board();

        assertTrue(board.placeShip(new Ship(ShipType.BOAT, new Coordinate(0, 0), Orientation.HORIZONTAL)));
        assertFalse(board.placeShip(new Ship(ShipType.BOAT, new Coordinate(1, 1), Orientation.HORIZONTAL)));
    }

    @Test
    void shootDetailedReturnsMissWhenNoShipExists() {
        Board board = new Board();

        ShotResolutionDTO result = board.shootDetailed(new Coordinate(4, 4));

        assertEquals(ResultantShot.MISS, result.result);
        assertFalse(result.sunkShip);
        assertNull(result.sunkShipType);
    }

    @Test
    void shootDetailedReturnsHitWhenShipIsHitButNotSunk() {
        Board board = new Board();
        board.placeShip(new Ship(ShipType.FRIGATE, new Coordinate(2, 2), Orientation.HORIZONTAL));

        ShotResolutionDTO result = board.shootDetailed(new Coordinate(2, 2));

        assertEquals(ResultantShot.HIT, result.result);
        assertFalse(result.sunkShip);
        assertNull(result.sunkShipType);
        assertEquals(1, result.remainingShips);
    }

    @Test
    void shootDetailedReturnsSunkWithShipTypeAndRemainingShips() {
        Board board = new Board();
        board.placeShip(new Ship(ShipType.BOAT, new Coordinate(1, 1), Orientation.HORIZONTAL));
        board.placeShip(new Ship(ShipType.FRIGATE, new Coordinate(4, 4), Orientation.HORIZONTAL));

        assertEquals(ResultantShot.HIT, board.shootDetailed(new Coordinate(1, 1)).result);

        ShotResolutionDTO result = board.shootDetailed(new Coordinate(1, 2));

        assertEquals(ResultantShot.SUNK, result.result);
        assertTrue(result.sunkShip);
        assertEquals(ShipType.BOAT, result.sunkShipType);
        assertEquals(1, result.remainingShips);
    }

    @Test
    void repeatedShotReturnsRepeated() {
        Board board = new Board();
        board.placeShip(new Ship(ShipType.BOAT, new Coordinate(0, 0), Orientation.HORIZONTAL));

        board.shootDetailed(new Coordinate(0, 0));
        ShotResolutionDTO repeated = board.shootDetailed(new Coordinate(0, 0));

        assertEquals(ResultantShot.REPEATED, repeated.result);
    }

    @Test
    void allSunkReturnsTrueOnlyWhenEveryShipIsDestroyed() {
        Board board = new Board();
        board.placeShip(new Ship(ShipType.BOAT, new Coordinate(0, 0), Orientation.HORIZONTAL));

        assertFalse(board.allSunk());

        board.shootDetailed(new Coordinate(0, 0));
        board.shootDetailed(new Coordinate(0, 1));

        assertTrue(board.allSunk());
    }
}