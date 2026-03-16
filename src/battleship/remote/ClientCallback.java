package battleship.remote;

import battleship.dto.GameStatusDTO;
import battleship.dto.ShipDTO;
import battleship.model.ResultantShot;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ClientCallback extends Remote {
    void notifyGameStatus(GameStatusDTO status) throws RemoteException;

    void notifyTurnResult(String shooter, ShipDTO shot, java.util.List<String> details) throws RemoteException;

    void notifyLog(String message) throws RemoteException;

    ResultantShot resolveIncomingShot(int row, int column) throws RemoteException;

    boolean hasLost() throws RemoteException;
}
