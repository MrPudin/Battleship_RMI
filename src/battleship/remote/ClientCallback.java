package battleship.remote;

import battleship.model.ResultantShot;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ClientCallback extends Remote {
    void notificar(String mensaje) throws RemoteException;

    ResultantShot resolveIncomingShot(int row, int column) throws RemoteException;

    boolean hasLost() throws RemoteException;
}