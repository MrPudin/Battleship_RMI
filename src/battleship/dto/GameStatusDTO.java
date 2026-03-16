package battleship.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class GameStatusDTO implements Serializable {
    public String message;

    public String roomName;
    public String phase;

    public String yourRole;
    public boolean youAreAlive;

    public boolean started;
    public boolean finished;
    public String winner;

    public int maxPlayers;
    public int currentPlayers;

    public List<String> allUsers;
    public List<String> alivePlayers;
    public List<String> readyPlayers;

    public GameStatusDTO() {
        this.allUsers = new ArrayList<>();
        this.alivePlayers = new ArrayList<>();
        this.readyPlayers = new ArrayList<>();
    }
}