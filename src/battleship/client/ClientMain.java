package battleship.client;

import battleship.dto.ShipDTO;
import battleship.model.ResultantShot;
import battleship.remote.BattleshipServer;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class ClientMain {
    public static void main(String[] args) {
        try {
            Registry registry = LocateRegistry.getRegistry("localhost", 1099);
            BattleshipServer server = (BattleshipServer) registry.lookup("BattleshipServer");

            Scanner sc = new Scanner(System.in);

            System.out.print("Nombre de jugador: ");
            String nombre = sc.nextLine();

            ClientCallbackImpl callback = new ClientCallbackImpl();
            boolean registrado = server.registerPlayer(nombre, callback);

            if (!registrado) {
                System.out.println("No se pudo registrar el jugador");
                return;
            }

            System.out.println("Jugador registrado");

            while (true) {
                System.out.println("\n1. Crear partida");
                System.out.println("2. Colocar barcos");
                System.out.println("3. Disparar");
                System.out.println("4. Ver turno actual");
                System.out.println("5. Ver si la partida ha empezado");
                System.out.println("0. Salir");
                System.out.print("Opción: ");

                String op = sc.nextLine();

                switch (op) {
                    case "1":
                        System.out.print("Nombre del rival: ");
                        String rival = sc.nextLine();
                        System.out.println(server.createGame(nombre, rival)
                                ? "Partida creada"
                                : "No se pudo crear");
                        break;

                    case "2":
                        List<ShipDTO> barcos = new ArrayList<>();

                        // Nombres corregidos para que coincidan con ShipType
                        barcos.add(new ShipDTO("BOAT", 0, 0, "HORIZONTAL"));
                        barcos.add(new ShipDTO("BOAT", 0, 3, "HORIZONTAL"));
                        barcos.add(new ShipDTO("BOAT", 0, 6, "HORIZONTAL"));
                        barcos.add(new ShipDTO("BOAT", 2, 0, "HORIZONTAL"));

                        barcos.add(new ShipDTO("FRIGATE", 2, 3, "HORIZONTAL"));
                        barcos.add(new ShipDTO("FRIGATE", 4, 0, "HORIZONTAL"));
                        barcos.add(new ShipDTO("FRIGATE", 4, 4, "HORIZONTAL"));

                        barcos.add(new ShipDTO("CRUISER", 6, 0, "HORIZONTAL"));
                        barcos.add(new ShipDTO("CRUISER", 8, 0, "HORIZONTAL"));

                        barcos.add(new ShipDTO("AIRCRAFTCARRIER", 8, 5, "HORIZONTAL"));

                        System.out.println(server.putShips(nombre, barcos)
                                ? "Barcos colocados"
                                : "No válidos");
                        break;

                    case "3":
                        System.out.print("Fila: ");
                        int fila = Integer.parseInt(sc.nextLine());

                        System.out.print("Columna: ");
                        int columna = Integer.parseInt(sc.nextLine());

                        ResultantShot resultado = server.shot(nombre, fila, columna);
                        System.out.println("Resultado: " + resultado);
                        break;

                    case "4":
                        System.out.println("Turno actual: " + server.obtainActualTurn(nombre));
                        break;

                    case "5":
                        System.out.println("¿Partida empezada?: " + server.startedGame(nombre));
                        break;

                    case "0":
                        return;

                    default:
                        System.out.println("Opción inválida");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}