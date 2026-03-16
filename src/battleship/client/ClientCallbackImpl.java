package battleship.client;

import battleship.dto.GameStatusDTO;
import battleship.dto.ShipDTO;
import battleship.model.Board;
import battleship.model.Coordinate;
import battleship.model.ResultantShot;
import battleship.remote.ClientCallback;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class ClientCallbackImpl extends UnicastRemoteObject implements ClientCallback {

    private Board localBoard;

    public ClientCallbackImpl() throws RemoteException {
        super();
    }

    public void setLocalBoard(Board localBoard) {
        this.localBoard = localBoard;
    }

    @Override
    public void notifyGameStatus(GameStatusDTO status) throws RemoteException {
        if (status == null) {
            return;
        }
        if (status.message != null && !status.message.isBlank()) {
            System.out.println("\n[NOTIFICACIÃ“N] " + status.message);
        }
        if (status.finished && status.winner != null && !status.winner.isBlank()) {
            System.out.println("[ESTADO] Ganador: " + status.winner);
        }
    }

    @Override
    public void notifyTurnResult(String shooter, ShipDTO shot, java.util.List<String> details) throws RemoteException {
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

        System.out.println(sb.toString());
    }

    @Override
    public void notifyLog(String message) throws RemoteException {
        if (message == null || message.isBlank()) {
            return;
        }
        System.out.println(message);
    }

    @Override
    public ResultantShot resolveIncomingShot(int row, int column) throws RemoteException {
        if (localBoard == null) {
            return ResultantShot.MISS;
        }

        return localBoard.shoot(new Coordinate(row, column));
    }

    @Override
    public boolean hasLost() throws RemoteException {
        return localBoard != null && localBoard.allSunk();
    }
}
