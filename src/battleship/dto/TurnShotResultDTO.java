package battleship.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class TurnShotResultDTO implements Serializable {
    public String shooter;
    public ShipDTO shot;
    public List<String> details;

    public TurnShotResultDTO() {
        this.details = new ArrayList<>();
    }

    public TurnShotResultDTO(String shooter, ShipDTO shot, List<String> details) {
        this.shooter = shooter;
        this.shot = shot;
        this.details = details != null ? new ArrayList<>(details) : new ArrayList<>();
    }
}