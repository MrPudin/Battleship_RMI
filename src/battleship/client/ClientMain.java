package battleship.client;

import battleship.model.Board;
import battleship.model.Coordinate;
import battleship.model.Orientation;
import battleship.model.Ship;
import battleship.model.ShipType;
import battleship.remote.BattleshipServer;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;

public class ClientMain {

    private static Board localBoard = null;

    public static void main(String[] args) {
        try {
            String serverHost = args.length > 0 ? args[0] : "127.0.0.1";
            int registryPort = args.length > 1 ? Integer.parseInt(args[1]) : 1099;
            String clientHost = args.length > 2 ? args[2] : "127.0.0.1";
            int callbackPort = args.length > 3 ? Integer.parseInt(args[3]) : 1200;

            System.setProperty("java.rmi.server.hostname", clientHost);

            Registry registry = LocateRegistry.getRegistry(serverHost, registryPort);
            BattleshipServer server = (BattleshipServer) registry.lookup("BattleshipServer");

            Scanner sc = new Scanner(System.in);

            System.out.print("Nombre de usuario: ");
            String username = sc.nextLine().trim();

            if (username.isEmpty()) {
                System.out.println("Nombre inválido");
                return;
            }

            ClientCallbackImpl callback = new ClientCallbackImpl(username, callbackPort);
            boolean registered = server.registerPlayer(username, callback);

            if (!registered) {
                System.out.println("No se pudo registrar el usuario");
                return;
            }

            callback.notifyLog("Cliente conectado. Crea o únete a una sala para empezar.");

            boolean running = true;

            while (running) {
                System.out.print("\nOpción> ");
                String option = sc.nextLine().trim();

                switch (option) {
                    case "1" -> {
                        System.out.print("Nombre de la sala: ");
                        String roomName = sc.nextLine().trim();

                        System.out.print("Número máximo de jugadores (2-4): ");
                        int maxPlayers;
                        try {
                            maxPlayers = Integer.parseInt(sc.nextLine().trim());
                        } catch (NumberFormatException e) {
                            callback.notifyLog("Número inválido");
                            break;
                        }

                        boolean created = server.newRoom(username, roomName, maxPlayers);
                        callback.notifyLog(created ? "Sala creada correctamente" : "No se pudo crear la sala");
                    }

                    case "2" -> {
                        System.out.print("Nombre de la sala: ");
                        String joinRoomName = sc.nextLine().trim();

                        System.out.print("Rol (PLAYER / SPECTATOR / ADMIN): ");
                        String role = sc.nextLine().trim().toUpperCase();

                        boolean joined = server.joinRoom(username, joinRoomName, role);
                        callback.notifyLog(joined ? "Te has unido a la sala" : "No se pudo unir a la sala");
                    }

                    case "3" -> {
                        boolean left = server.leaveRoom(username);
                        if (left) {
                            localBoard = null;
                            callback.setLocalBoard(null);
                        }
                        callback.notifyLog(left ? "Has salido de la sala" : "No se pudo salir de la sala");
                    }

                    case "4" -> {
                        localBoard = buildDefaultBoard();
                        if (localBoard != null) {
                            callback.setLocalBoard(localBoard);
                            callback.notifyLog("Barcos colocados localmente en el cliente");
                        } else {
                            callback.notifyLog("No se pudo colocar el tablero local");
                        }
                    }

                    case "5" -> {
                        if (localBoard == null) {
                            callback.notifyLog("Primero debes colocar los barcos localmente");
                            break;
                        }

                        boolean ready = server.playerReady(username);
                        callback.notifyLog(ready ? "Marcado como listo" : "No se pudo marcar como listo");
                    }

                    case "6" -> {
                        System.out.print("Fila: ");
                        int row;
                        try {
                            row = Integer.parseInt(sc.nextLine().trim());
                        } catch (NumberFormatException e) {
                            callback.notifyLog("Fila inválida");
                            break;
                        }

                        System.out.print("Columna: ");
                        int col;
                        try {
                            col = Integer.parseInt(sc.nextLine().trim());
                        } catch (NumberFormatException e) {
                            callback.notifyLog("Columna inválida");
                            break;
                        }

                        callback.registerPendingShot(row, col);

                        boolean shotSubmitted = server.submitShot(username, row, col);

                        if (!shotSubmitted) {
                            callback.cancelPendingShot(row, col);
                            callback.notifyLog("No se pudo enviar el disparo");
                        }
                    }

                    case "0" -> {
                        boolean exited = server.exit(username);
                        callback.notifyLog(exited ? "Sesión cerrada" : "No se pudo cerrar la sesión");
                        running = false;
                    }

                    default -> callback.notifyLog("Opción inválida");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static Board buildDefaultBoard() {
        Board board = new Board();
        boolean ok = true;

        ok &= board.placeShip(new Ship(ShipType.BOAT, new Coordinate(0, 0), Orientation.HORIZONTAL));
        ok &= board.placeShip(new Ship(ShipType.FRIGATE, new Coordinate(2, 2), Orientation.HORIZONTAL));
        ok &= board.placeShip(new Ship(ShipType.CRUISER, new Coordinate(5, 0), Orientation.HORIZONTAL));
        ok &= board.placeShip(new Ship(ShipType.AIRCRAFTCARRIER, new Coordinate(7, 4), Orientation.HORIZONTAL));

        return ok ? board : null;
    }
}