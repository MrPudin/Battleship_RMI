package src.battleship.testdoubles;

import battleship.dto.GameStatusDTO;
import battleship.dto.ShipDTO;
import battleship.model.ResultantShot;
import battleship.remote.ClientCallback;

import java.rmi.RemoteException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Queue;

public class ClientCallStub implements ClientCallback {

    private final List<String> messages = new ArrayList<>();
    private final Queue<ResultantShot> queuedShotResults = new ArrayDeque<>();
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
    public void notifyTurnResult(String shooter, ShipDTO shot, List<String> details) throws RemoteException {
        if (shot == null) {
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Resultado del disparo de ")
                .append(shooter)
                .append(" en (")
                .append(shot.row)
                .append(",")
                .append(shot.column)
                .append("): ")
                .append(shot.type);

        if (details != null && !details.isEmpty()) {
            sb.append(" -> ").append(String.join(" | ", details));
        }

        messages.add(sb.toString());
    }

    @Override
    public void notifyLog(String message) throws RemoteException {
        if (message != null) {
            messages.add(message);
        }
    }

    @Override
    public ResultantShot resolveIncomingShot(int row, int column) throws RemoteException {
        if (queuedShotResults.isEmpty()) {
            return ResultantShot.MISS;
        }
        return queuedShotResults.poll();
    }

    @Override
    public boolean hasLost() throws RemoteException {
        return lost;
    }

    public void enqueueShotResult(ResultantShot result) {
        queuedShotResults.add(result);
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
