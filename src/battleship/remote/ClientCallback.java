package battleship.remote;

import battleship.dto.GameStatusDTO;
import battleship.dto.ShipDTO;
import battleship.dto.ShotResolutionDTO;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface ClientCallback extends Remote {
    void notifyGameStatus(GameStatusDTO status) throws RemoteException;

    void notifyTurnResult(String shooter, ShipDTO shot, List<String> details) throws RemoteException;

    void notifyTurnResolved() throws RemoteException;

    void notifyLog(String message) throws RemoteException;

    ShotResolutionDTO resolveIncomingShot(int row, int column) throws RemoteException;

    boolean hasLost() throws RemoteException;
}