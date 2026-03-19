package battleship.remote;

import battleship.dto.GameStatusDTO;
import battleship.dto.ShotResolutionDTO;
import battleship.dto.TurnShotResultDTO;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface ClientCallback extends Remote {
    void notifyGameStatus(GameStatusDTO status) throws RemoteException;

    void notifyTurnBatch(List<TurnShotResultDTO> results) throws RemoteException;

    void notifyLog(String message) throws RemoteException;

    ShotResolutionDTO resolveIncomingShot(int row, int column) throws RemoteException;

    boolean hasLost() throws RemoteException;
}