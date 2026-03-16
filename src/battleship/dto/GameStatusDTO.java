package battleship.dto;

import java.io.Serializable;

public class GameStatusDTO implements Serializable {
    public String message;
    public String player;
    public String rival;
    public String currentTurn;
    public boolean started;
    public boolean finished;
    public String winner;

    public GameStatusDTO() {
    }
}
