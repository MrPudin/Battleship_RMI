package battleship.client;

import battleship.dto.GameStatusDTO;
import battleship.dto.ShipDTO;
import battleship.dto.ShotResolutionDTO;
import battleship.model.Board;
import battleship.model.Coordinate;
import battleship.remote.ClientCallback;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ClientCallbackImpl extends UnicastRemoteObject implements ClientCallback {

    private final String username;
    private Board localBoard;
    private GameStatusDTO lastStatus;

    private final List<String> eventLog = new ArrayList<>();
    private final Map<String, String> firedShots = new LinkedHashMap<>();

    private final Map<String, String> pendingResolvedShots = new LinkedHashMap<>();
    private final List<String> pendingTurnEvents = new ArrayList<>();

    public ClientCallbackImpl(String username, int callbackPort) throws RemoteException {
        super(callbackPort);
        this.username = username;
    }

    public void setLocalBoard(Board localBoard) {
        this.localBoard = localBoard;
        if (localBoard != null) {
            addEvent("Tablero local actualizado.");
        } else {
            addEvent("Tablero local limpiado.");
        }
        render();
    }

    public void registerPendingShot(int row, int col) {
        firedShots.put(key(row, col), "PENDIENTE");
        addEvent("Disparo enviado a (" + row + "," + col + "), pendiente de resolución.");
        render();
    }

    @Override
    public void notifyGameStatus(GameStatusDTO status) throws RemoteException {
        if (status == null) {
            return;
        }

        this.lastStatus = status;

        if (status.message != null && !status.message.isBlank()) {
            addEvent(status.message);
        }

        render();
    }

    @Override
    public void notifyTurnResult(String shooter, ShipDTO shot, List<String> details) throws RemoteException {
        if (shot == null) {
            return;
        }

        if (shooter != null && shooter.equals(username)) {
            pendingResolvedShots.put(key(shot.row, shot.column), shot.type);
        }

        pendingTurnEvents.add("Disparo de " + shooter + " en (" + shot.row + "," + shot.column + ") -> " + shot.type);

        if (details != null && !details.isEmpty()) {
            pendingTurnEvents.addAll(details);
        }
    }

    @Override
    public void notifyTurnResolved() throws RemoteException {
        for (Map.Entry<String, String> entry : pendingResolvedShots.entrySet()) {
            firedShots.put(entry.getKey(), entry.getValue());
        }
        pendingResolvedShots.clear();

        for (String event : pendingTurnEvents) {
            addEvent(event);
        }
        pendingTurnEvents.clear();

        render();
    }

    @Override
    public void notifyLog(String message) throws RemoteException {
        if (message == null || message.isBlank()) {
            return;
        }

        addEvent(message);
        render();
    }

    @Override
    public ShotResolutionDTO resolveIncomingShot(int row, int column) throws RemoteException {
        if (localBoard == null) {
            return null;
        }

        ShotResolutionDTO resolution = localBoard.shootDetailed(new Coordinate(row, column));
        addEvent("Has recibido un disparo en (" + row + "," + column + ") -> " + resolution.result);
        render();
        return resolution;
    }

    @Override
    public boolean hasLost() throws RemoteException {
        return localBoard != null && localBoard.allSunk();
    }

    private void render() {
        TerminalUI.render(username, localBoard, firedShots, eventLog, lastStatus);
    }

    private void addEvent(String message) {
        if (message != null && !message.isBlank()) {
            eventLog.add(message);
        }
    }

    private String key(int row, int col) {
        return row + "," + col;
    }
}