package battleship.server;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class ServerMain {
    public static void main(String[] args) {
        try {
            Registry registry = LocateRegistry.createRegistry(1099);
            BattleshipServerImpl server = new BattleshipServerImpl();
            registry.rebind("BattleshipServer", server);
            System.out.println("Servidor RMI listo en puerto 1099");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}