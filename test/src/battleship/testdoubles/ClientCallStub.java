package battleship.testdoubles;

import battleship.dto.GameStatusDTO;
import battleship.dto.ShotResolutionDTO;
import battleship.dto.TurnShotResultDTO;
import battleship.model.ResultantShot;
import battleship.model.ShipType;
import battleship.remote.ClientCallback;

import java.rmi.RemoteException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Queue;

public class ClientCallStub implements ClientCallback {

    private final List<String> messages = new ArrayList<>();
    private final Queue<ShotResolutionDTO> queuedShotResults = new ArrayDeque<>();
    private boolean lost = false;

    @Override
    public void notifyGameStatus(GameStatusDTO status) throws RemoteException {
        if (status == null) {
            return;
        }

        if (status.message != null) {
            messages.add(status.message);
        }
    }

    @Override
    public void notifyTurnBatch(List<TurnShotResultDTO> results) throws RemoteException {
        if (results == null || results.isEmpty()) {
            return;
        }

        for (TurnShotResultDTO result : results) {
            if (result == null || result.shot == null) {
                continue;
            }

            StringBuilder sb = new StringBuilder();
            sb.append("Resultado del disparo de ")
                    .append(result.shooter)
                    .append(" en (")
                    .append(result.shot.row)
                    .append(",")
                    .append(result.shot.column)
                    .append("): ")
                    .append(result.shot.type);

            if (result.details != null && !result.details.isEmpty()) {
                sb.append(" -> ").append(String.join(" | ", result.details));
            }

            messages.add(sb.toString());
        }
    }

    @Override
    public void notifyLog(String message) throws RemoteException {
        if (message != null) {
            messages.add(message);
        }
    }

    @Override
    public ShotResolutionDTO resolveIncomingShot(int row, int column) throws RemoteException {
        if (queuedShotResults.isEmpty()) {
            return new ShotResolutionDTO(ResultantShot.MISS, false, null, 0);
        }
        return queuedShotResults.poll();
    }

    @Override
    public boolean hasLost() throws RemoteException {
        return lost;
    }

    public void enqueueShotResult(ShotResolutionDTO result) {
        queuedShotResults.add(result);
    }

    public void enqueueShotResult(ResultantShot result) {
        queuedShotResults.add(new ShotResolutionDTO(result, false, null, 0));
    }

    public void enqueueSunkShotResult(ShipType shipType, int remainingShips) {
        queuedShotResults.add(new ShotResolutionDTO(ResultantShot.SUNK, true, shipType, remainingShips));
    }

    public void setLost(boolean lost) {
        this.lost = lost;
    }

    public List<String> getMessages() {
        return Collections.unmodifiableList(messages);
    }

    public boolean containsMessage(String text) {
        for (String message : messages) {
            if (message != null && message.contains(text)) {
                return true;
            }
        }
        return false;
    }

    public int messageCount() {
        return messages.size();
    }

    public void clearMessages() {
        messages.clear();
    }
}