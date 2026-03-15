package battleship.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Room implements Serializable {
    private final String name;
    private final int maxPlayers;

    private final Map<String, RoomRole> users = new HashMap<>();
    private final Set<String> alivePlayers = new HashSet<>();
    private final Set<String> readyPlayers = new HashSet<>();

    private GamePhase phase = GamePhase.WAITING_PLAYERS;

    public Room(String name, int maxPlayers) {
        this.name = name;
        this.maxPlayers = maxPlayers;
    }

    public String getName() {
        return name;
    }

    public int getMaxPlayers() {
        return maxPlayers;
    }

    public Map<String, RoomRole> getUsers() {
        return users;
    }

    public Set<String> getAlivePlayers() {
        return alivePlayers;
    }

    public Set<String> getReadyPlayers() {
        return readyPlayers;
    }

    public GamePhase getPhase() {
        return phase;
    }

    public void setPhase(GamePhase phase) {
        this.phase = phase;
    }

    public boolean addUser(String username, RoomRole role) {
        if (users.containsKey(username)) return false;

        if (role == RoomRole.PLAYER && countPlayers() >= maxPlayers) {
            return false;
        }

        users.put(username, role);

        if (role == RoomRole.PLAYER) {
            alivePlayers.add(username);
        }

        return true;
    }

    public void removeUser(String username) {
        RoomRole role = users.remove(username);

        if (role == RoomRole.PLAYER) {
            alivePlayers.remove(username);
        }

        readyPlayers.remove(username);
    }

    public int countPlayers() {
        int count = 0;
        for (RoomRole role : users.values()) {
            if (role == RoomRole.PLAYER) count++;
        }
        return count;
    }

    public boolean isFullForPlayers() {
        return countPlayers() == maxPlayers;
    }

    public boolean isEmpty() {
        return users.isEmpty();
    }

    public void convertPlayerToSpectator(String username) {
        if (users.get(username) == RoomRole.PLAYER) {
            users.put(username, RoomRole.SPECTATOR);
            alivePlayers.remove(username);
            readyPlayers.remove(username);
        }
    }

    public boolean markPlayerReady(String username) {
        if (users.get(username) != RoomRole.PLAYER) return false;
        if (phase != GamePhase.PLACING_SHIPS) return false;

        readyPlayers.add(username);
        return true;
    }

    public boolean areAllPlayersReady() {
        for (Map.Entry<String, RoomRole> entry : users.entrySet()) {
            if (entry.getValue() == RoomRole.PLAYER && !readyPlayers.contains(entry.getKey())) {
                return false;
            }
        }
        return countPlayers() > 0;
    }

    public void resetReadyPlayers() {
        readyPlayers.clear();
    }
}