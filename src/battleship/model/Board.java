package battleship.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Board implements Serializable {
    public static final int SIZE = 10;

    private final List<Ship> ships = new ArrayList<>();
    private final List<Coordinate> disparosRecibidos = new ArrayList<>();

    public boolean colocarBarco(Ship ship) {
        if (!cabe(ship)) return false;
        if (colisiona(ship)) return false;
        ships.add(ship);
        return true;
    }

    private boolean cabe(Ship ship) {
        for (Coordinate c : ship.getCells()) {
            if (c.getRow() < 0 || c.getRow() >= SIZE || c.getColumn() < 0 || c.getColumn() >= SIZE) {
                return false;
            }
        }
        return true;
    }

    private boolean colisiona(Ship newShip) {
        for (Coordinate newCoordinate : newShip.getCells()) {
            for (Ship ship : ships) {
                for (Coordinate existente : ship.getCells()) {
                    if (Math.abs(newCoordinate.getRow() - existente.getRow()) <= 1 &&
                            Math.abs(newCoordinate.getColumn() - existente.getColumn()) <= 1) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public ResultantShot disparar(Coordinate c) {
        boolean repetido = disparosRecibidos.stream()
                .anyMatch(x -> x.getRow() == c.getRow() && x.getColumn() == c.getColumn());

        if (repetido) return ResultantShot.REPEATED;

        disparosRecibidos.add(c);

        for (Ship ship : ships) {
            if (ship.registerImpact(c)) {
                return ship.itsSunk() ? ResultantShot.SUNK : ResultantShot.HIT;
            }
        }

        return ResultantShot.MISS;
    }

    public boolean allSunk() {
        return !ships.isEmpty() && ships.stream().allMatch(Ship::itsSunk);
    }

    public List<Ship> getShips() {
        return ships;
    }

    public List<Coordinate> getDisparosRecibidos() {
        return disparosRecibidos;
    }
}