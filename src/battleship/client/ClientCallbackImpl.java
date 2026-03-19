package battleship.client;

import battleship.dto.GameStatusDTO;
import battleship.dto.ShotResolutionDTO;
import battleship.dto.TurnShotResultDTO;
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
    public synchronized void notifyGameStatus(GameStatusDTO status) throws RemoteException {
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
    public synchronized void notifyTurnBatch(List<TurnShotResultDTO> results) throws RemoteException {
        if (results == null || results.isEmpty()) {
            return;
        }

        for (TurnShotResultDTO result : results) {
            if (result == null || result.shot == null) {
                continue;
            }

            if (result.shooter != null && result.shooter.equals(username)) {
                firedShots.put(key(result.shot.row, result.shot.column), result.shot.type);
            }

            addEvent("Disparo de " + result.shooter + " en (" + result.shot.row + "," + result.shot.column + ") -> " + result.shot.type);

            if (result.details != null && !result.details.isEmpty()) {
                for (String detail : result.details) {
                    addEvent(detail);
                }
            }
        }

        render();
    }

    @Override
    public synchronized void notifyLog(String message) throws RemoteException {
        if (message == null || message.isBlank()) {
            return;
        }

        addEvent(message);
        render();
    }

    @Override
    public synchronized ShotResolutionDTO resolveIncomingShot(int row, int column) throws RemoteException {
        if (localBoard == null) {
            return null;
        }

        ShotResolutionDTO resolution = localBoard.shootDetailed(new Coordinate(row, column));
        addEvent("Has recibido un disparo en (" + row + "," + column + ") -> " + resolution.result);
        render();
        return resolution;
    }

    public synchronized void cancelPendingShot(int row, int col) {
        firedShots.remove(key(row, col));
        addEvent("Se canceló el disparo en (" + row + "," + col + ").");
        render();
    }

    @Override
    public synchronized boolean hasLost() throws RemoteException {
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