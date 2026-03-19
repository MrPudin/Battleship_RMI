package battleship.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ShipDTO implements Serializable {
    public String type;
    public int row;
    public int column;
    public String orientation;

    public List<String> sunkEvents;

    public ShipDTO(String type, int row, int column, String orientation) {
        this.type = type;
        this.row = row;
        this.column = column;
        this.orientation = orientation;
        this.sunkEvents = new ArrayList<>();
    }
}