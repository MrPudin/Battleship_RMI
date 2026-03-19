package battleship.dto;

import battleship.model.ResultantShot;
import battleship.model.ShipType;

import java.io.Serializable;

public class ShotResolutionDTO implements Serializable {
    public ResultantShot result;
    public boolean sunkShip;
    public ShipType sunkShipType;
    public int remainingShips;

    public ShotResolutionDTO() {
    }

    public ShotResolutionDTO(ResultantShot result, boolean sunkShip, ShipType sunkShipType, int remainingShips) {
        this.result = result;
        this.sunkShip = sunkShip;
        this.sunkShipType = sunkShipType;
        this.remainingShips = remainingShips;
    }
}