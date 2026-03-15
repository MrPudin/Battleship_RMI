package battleship.remote;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface BattleshipServer extends Remote {
    boolean registerPlayer(String username, ClientCallback callback) throws RemoteException;

    boolean newRoom(String username, String roomName, int maxPlayers) throws RemoteException;

    boolean joinRoom(String username, String roomName, String role) throws RemoteException;

    boolean leaveRoom(String username) throws RemoteException;

    boolean exit(String username) throws RemoteException;

    boolean playerReady(String username) throws RemoteException;
}