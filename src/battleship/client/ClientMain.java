package battleship.client;

import battleship.remote.BattleshipServer;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;

public class ClientMain {
    public static void main(String[] args) {
        try {
            Registry registry = LocateRegistry.getRegistry("localhost", 1099);
            BattleshipServer server = (BattleshipServer) registry.lookup("BattleshipServer");

            Scanner sc = new Scanner(System.in);

            System.out.print("Nombre de usuario: ");
            String username = sc.nextLine().trim();

            if (username.isEmpty()) {
                System.out.println("Nombre inválido");
                return;
            }

            ClientCallbackImpl callback = new ClientCallbackImpl();
            boolean registered = server.registerPlayer(username, callback);

            if (!registered) {
                System.out.println("No se pudo registrar el usuario");
                return;
            }

            System.out.println("Usuario registrado correctamente");

            boolean running = true;

            while (running) {
                System.out.println("\n===== MENÚ =====");
                System.out.println("1. NewRoom");
                System.out.println("2. JoinRoom");
                System.out.println("3. LeaveRoom");
                System.out.println("4. Exit");
                System.out.print("Opción: ");

                String option = sc.nextLine().trim();

                switch (option) {
                    case "1":
                        System.out.print("Nombre de la sala: ");
                        String roomName = sc.nextLine().trim();

                        System.out.print("Número máximo de jugadores (2-4): ");
                        int maxPlayers;
                        try {
                            maxPlayers = Integer.parseInt(sc.nextLine().trim());
                        } catch (NumberFormatException e) {
                            System.out.println("Número inválido");
                            break;
                        }

                        boolean created = server.newRoom(username, roomName, maxPlayers);
                        System.out.println(created ? "Sala creada correctamente" : "No se pudo crear la sala");
                        break;

                    case "2":
                        System.out.print("Nombre de la sala: ");
                        String joinRoomName = sc.nextLine().trim();

                        System.out.print("Rol (PLAYER / SPECTATOR / ADMIN): ");
                        String role = sc.nextLine().trim().toUpperCase();

                        boolean joined = server.joinRoom(username, joinRoomName, role);
                        System.out.println(joined ? "Te has unido a la sala" : "No se pudo unir a la sala");
                        break;

                    case "3":
                        boolean left = server.leaveRoom(username);
                        System.out.println(left ? "Has salido de la sala" : "No se pudo salir de la sala");
                        break;

                    case "4":
                        boolean exited = server.exit(username);
                        System.out.println(exited ? "Sesión cerrada" : "No se pudo cerrar la sesión");
                        running = false;
                        break;

                    default:
                        System.out.println("Opción inválida");
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}