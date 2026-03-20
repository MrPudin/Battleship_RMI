package battleship.model;

import battleship.remote.ClientCallback;

public class UserSession {
    private final String username;
    private final ClientCallback callback;
    private String roomName;

    public UserSession(String username, ClientCallback callback) {
        this.username = username;
        this.callback = callback;
    }

    public String getUsername() {
        return username;
    }

    public ClientCallback getCallback() {
        return callback;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public boolean isInRoom() {
        return roomName != null;
    }
}