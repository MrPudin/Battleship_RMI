package battleship.client;

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
    public void notificar(String mensaje) throws RemoteException {
        System.out.println("\n[NOTIFICACIÓN] " + mensaje);
    }

    @Override
    public ResultantShot resolveIncomingShot(int row, int column) throws RemoteException {
        if (localBoard == null) {
            return ResultantShot.MISS;
        }

        return localBoard.disparar(new Coordinate(row, column));
    }

    @Override
    public boolean hasLost() throws RemoteException {
        return localBoard != null && localBoard.allSunk();
    }
}