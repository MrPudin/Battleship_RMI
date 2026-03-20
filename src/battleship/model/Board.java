package battleship.model;

import battleship.dto.ShotResolutionDTO;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Board implements Serializable {
    public static final int SIZE = 10;

    private final List<Ship> ships = new ArrayList<>();
    private final List<Coordinate> receivedShots = new ArrayList<>();

    public boolean placeShip(Ship ship) {
        if (!fits(ship)) return false;
        if (collides(ship)) return false;
        ships.add(ship);
        return true;
    }

    private boolean fits(Ship ship) {
        for (Coordinate c : ship.getCells()) {
            if (c.getRow() < 0 || c.getRow() >= SIZE || c.getColumn() < 0 || c.getColumn() >= SIZE) {
                return false;
            }
        }
        return true;
    }

    private boolean collides(Ship newShip) {
        for (Coordinate newCoordinate : newShip.getCells()) {
            for (Ship ship : ships) {
                for (Coordinate existing : ship.getCells()) {
                    if (Math.abs(newCoordinate.getRow() - existing.getRow()) <= 1 &&
                            Math.abs(newCoordinate.getColumn() - existing.getColumn()) <= 1) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public ShotResolutionDTO shootDetailed(Coordinate c) {
        if (c.getRow() < 0 || c.getRow() >= SIZE || c.getColumn() < 0 || c.getColumn() >= SIZE) {
            return new ShotResolutionDTO(ResultantShot.MISS, false, null, countRemainingShips());
        }

        boolean repeated = receivedShots.stream()
                .anyMatch(x -> x.getRow() == c.getRow() && x.getColumn() == c.getColumn());

        if (repeated) {
            return new ShotResolutionDTO(ResultantShot.REPEATED, false, null, countRemainingShips());
        }

        receivedShots.add(c);

        for (Ship ship : ships) {
            if (ship.occupies(c)) {
                ship.registerImpact(c);

                if (ship.isSunk()) {
                    return new ShotResolutionDTO(
                            ResultantShot.SUNK,
                            true,
                            ship.getType(),
                            countRemainingShips()
                    );
                }

                return new ShotResolutionDTO(
                        ResultantShot.HIT,
                        false,
                        null,
                        countRemainingShips()
                );
            }
        }

        return new ShotResolutionDTO(ResultantShot.MISS, false, null, countRemainingShips());
    }

    public ResultantShot shoot(Coordinate c) {
        return shootDetailed(c).result;
    }

    public boolean allSunk() {
        return !ships.isEmpty() && ships.stream().allMatch(Ship::isSunk);
    }

    public int countRemainingShips() {
        int count = 0;
        for (Ship ship : ships) {
            if (!ship.isSunk()) {
                count++;
            }
        }
        return count;
    }

    public List<Ship> getShips() {
        return ships;
    }

    public List<Coordinate> getReceivedShots() {
        return receivedShots;
    }
}