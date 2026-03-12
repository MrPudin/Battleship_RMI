package battleship.client;

import battleship.remote.ClientCallback;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class ClientCallbackImpl extends UnicastRemoteObject implements ClientCallback {

    public ClientCallbackImpl() throws RemoteException {
        super();
    }

    @Override
    public void notificar(String mensaje) throws RemoteException {
        System.out.println("[SERVIDOR] " + mensaje);
    }
}