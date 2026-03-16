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

    public ClientCallbackImpl(int serverPort) throws RemoteException {
        super(serverPort);
    }

    public void setLocalBoard(Board localBoard) {
        this.localBoard = localBoard;
    }

    @Override
    public void notifyGameStatus(GameStatusDTO status) throws RemoteException {
        if (status == null) {
            return;
        }

        System.out.println("\n================ ESTADO ================");

        if (status.message != null && !status.message.isBlank()) {
            System.out.println("Mensaje: " + status.message);
        }

        if (status.roomName != null) {
            System.out.println("Sala: " + status.roomName);
        }

        if (status.phase != null) {
            System.out.println("Fase: " + status.phase);
        }

        if (status.yourRole != null) {
            System.out.println("Tu rol: " + status.yourRole);
        }

        System.out.println("Sigues vivo: " + (status.youAreAlive ? "Sí" : "No"));
        System.out.println("Empezada: " + (status.started ? "Sí" : "No"));
        System.out.println("Terminada: " + (status.finished ? "Sí" : "No"));

        if (status.winner != null && !status.winner.isBlank()) {
            System.out.println("Ganador: " + status.winner);
        }

        System.out.println("Jugadores actuales: " + status.currentPlayers + "/" + status.maxPlayers);

        if (status.allUsers != null && !status.allUsers.isEmpty()) {
            System.out.println("Usuarios en sala: " + String.join(", ", status.allUsers));
        }

        if (status.alivePlayers != null && !status.alivePlayers.isEmpty()) {
            System.out.println("Jugadores vivos: " + String.join(", ", status.alivePlayers));
        }

        if (status.readyPlayers != null && !status.readyPlayers.isEmpty()) {
            System.out.println("Jugadores listos: " + String.join(", ", status.readyPlayers));
        }

        System.out.println("========================================");
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
