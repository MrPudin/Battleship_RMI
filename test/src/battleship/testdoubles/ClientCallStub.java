package src.battleship.testdoubles;

import battleship.remote.ClientCallback;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ClientCallStub implements ClientCallback {

    private final List<String> messages = new ArrayList<>();

    @Override
    public void notificar(String mensaje) throws RemoteException {
        messages.add(mensaje);
    }

    public List<String> getMessages() {
        return Collections.unmodifiableList(messages);
    }

    public boolean containsMessage(String text) {
        for (String message : messages) {
            if (message != null && message.contains(text)) {
                return true;
            }
        }
        return false;
    }

    public int messageCount() {
        return messages.size();
    }

    public void clearMessages() {
        messages.clear();
    }
}