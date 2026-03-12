package battleship.remote;

import battleship.dto.ShipDTO;
import battleship.model.ResultantShot;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface BattleshipServer extends Remote {
    boolean registerPlayer(String name, ClientCallback callback) throws RemoteException;
    boolean createGame(String player1, String player2) throws RemoteException;
    boolean putShips(String player, List<ShipDTO> ships) throws RemoteException;
    ResultantShot shot(String player, int row, int column) throws RemoteException;
    String obtainActualTurn(String player) throws RemoteException;
    boolean startedGame(String player) throws RemoteException;
}