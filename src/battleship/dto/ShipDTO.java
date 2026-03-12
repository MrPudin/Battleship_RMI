package battleship.dto;

import java.io.Serializable;

public class ShipDTO implements Serializable {
    public String type;
    public int row;
    public int column;
    public String orientation;

    public ShipDTO() {
    }

    public ShipDTO(String type, int row, int column, String orientation) {
        this.type = type;
        this.row = row;
        this.column = column;
        this.orientation = orientation;
    }
}
