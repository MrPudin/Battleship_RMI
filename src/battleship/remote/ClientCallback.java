package battleship.remote;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ClientCallback extends Remote {
    void notificar(String mensaje) throws RemoteException;
}