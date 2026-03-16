package battleship.server;

import battleship.remote.BattleshipServer;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class ServerMain {
    public static void main(String[] args) {
        try {
            String bindHost = args.length > 0 ? args[0] : "127.0.0.1";
            int registryPort = args.length > 1 ? Integer.parseInt(args[1]) : 1099;
            int serverPort = args.length > 2 ? Integer.parseInt(args[2]) : 1100;

            System.setProperty("java.rmi.server.hostname", bindHost);

            Registry registry = LocateRegistry.createRegistry(registryPort);
            BattleshipServer server = new BattleshipServerImpl(serverPort);
            registry.rebind("BattleshipServer", server);

            System.out.println("Servidor RMI listo");
            System.out.println("Host anunciado: " + bindHost);
            System.out.println("Registry port: " + registryPort);
            System.out.println("Server port: " + serverPort);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}