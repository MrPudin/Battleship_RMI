package battleship.client;

import battleship.dto.GameStatusDTO;
import battleship.model.Board;
import battleship.model.Coordinate;
import battleship.model.Ship;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TerminalUI {

    public static final String RESET = "\033[0m";
    public static final String RED = "\033[31m";
    public static final String GREEN = "\033[32m";
    public static final String YELLOW = "\033[33m";
    public static final String BLUE = "\033[34m";
    public static final String CYAN = "\033[36m";
    public static final String WHITE = "\033[37m";
    public static final String BOLD = "\033[1m";

    public static void clearScreen() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    public static void render(
            String username,
            Board localBoard,
            Map<String, String> firedShots,
            List<String> eventLog,
            GameStatusDTO status
    ) {
        clearScreen();

        printHeader(username, status);
        System.out.println();

        boolean showBoards = status != null && status.roomName != null && !status.roomName.isBlank();

        if (showBoards) {
            List<String> ownBoard = buildOwnBoard(localBoard);
            List<String> enemyBoard = buildEnemyBoard(firedShots);

            int maxLines = Math.max(ownBoard.size(), enemyBoard.size());

            System.out.printf("%-50s | %-48s%n",
                    BOLD + "TU TABLERO" + RESET,
                    BOLD + "DISPAROS REALIZADOS" + RESET);
            System.out.println("-".repeat(107));

            for (int i = 0; i < maxLines; i++) {
                String left = i < ownBoard.size() ? ownBoard.get(i) : "";
                String right = i < enemyBoard.size() ? enemyBoard.get(i) : "";
                System.out.printf("%-50s | %-48s%n", left, right);
            }

            System.out.println();
            System.out.println("LEYENDA TABLERO PROPIO: "
                    + CYAN + "B" + RESET + "=barco  "
                    + RED + "X" + RESET + "=impacto recibido en barco  "
                    + BLUE + "o" + RESET + "=agua recibida  "
                    + WHITE + "." + RESET + "=vacío");
            System.out.println("LEYENDA DISPAROS: "
                    + RED + "X" + RESET + "=tocado/hundido  "
                    + BLUE + "o" + RESET + "=agua  "
                    + YELLOW + "*" + RESET + "=pendiente");
            System.out.println();
        } else {
            System.out.println(BOLD + "No estás dentro de una sala todavía." + RESET);
            System.out.println("Cuando entres o crees una sala, aparecerán los tableros.");
            System.out.println();
        }

        System.out.println(BOLD + "EVENTOS RECIENTES" + RESET);
        System.out.println("-".repeat(40));

        int start = Math.max(0, eventLog.size() - 10);
        for (int i = start; i < eventLog.size(); i++) {
            System.out.println(eventLog.get(i));
        }

        System.out.println();
        System.out.println(BOLD + "COMANDOS" + RESET);
        System.out.println("1 NewRoom   2 JoinRoom   3 LeaveRoom   4 Colocar barcos");
        System.out.println("5 Ready     6 Disparar   0 Exit");
    }

    private static void printHeader(String username, GameStatusDTO status) {
        System.out.println(BOLD + GREEN + "=== BATTLESHIP RMI ===" + RESET);
        System.out.println("Usuario: " + username);

        if (status == null) {
            System.out.println("Estado: sin datos de sala");
            return;
        }

        System.out.println("Sala: " + safe(status.roomName));
        System.out.println("Fase: " + safe(status.phase));
        System.out.println("Rol: " + safe(status.yourRole));
        System.out.println("Vivo: " + (status.youAreAlive ? "Sí" : "No"));
        System.out.println("Jugadores: " + status.currentPlayers + "/" + status.maxPlayers);

        if (status.winner != null && !status.winner.isBlank()) {
            System.out.println(RED + "Ganador: " + status.winner + RESET);
        }

        if (status.message != null && !status.message.isBlank()) {
            System.out.println("Último mensaje: " + status.message);
        }
    }

    private static List<String> buildOwnBoard(Board board) {
        List<String> lines = new ArrayList<>();
        lines.add(headerRow(true));

        for (int r = 0; r < Board.SIZE; r++) {
            StringBuilder line = new StringBuilder();
            line.append(String.format("%2d ", r));

            for (int c = 0; c < Board.SIZE; c++) {
                boolean wasShot = containsCoordinate(board != null ? board.getReceivedShots() : List.of(), r, c);
                boolean hasShip = board != null && hasShipAt(board, r, c);

                if (hasShip && wasShot) {
                    line.append(RED).append('X').append(RESET).append(' ');
                } else if (hasShip) {
                    line.append(CYAN).append('B').append(RESET).append(' ');
                } else if (wasShot) {
                    line.append(BLUE).append('o').append(RESET).append(' ');
                } else {
                    line.append(WHITE).append('.').append(RESET).append(' ');
                }
            }

            lines.add(line.toString());
        }

        return lines;
    }

    private static List<String> buildEnemyBoard(Map<String, String> firedShots) {
        List<String> lines = new ArrayList<>();
        lines.add(headerRow(true));

        for (int r = 0; r < Board.SIZE; r++) {
            StringBuilder line = new StringBuilder();
            line.append(String.format("%2d ", r));

            for (int c = 0; c < Board.SIZE; c++) {
                String key = key(r, c);
                String result = firedShots.get(key);

                if (result == null) {
                    line.append(WHITE).append('.').append(RESET).append(' ');
                } else {
                    switch (result) {
                        case "AGUA" -> line.append(BLUE).append('o').append(RESET).append(' ');
                        case "TOCADO", "HUNDIDO" -> line.append(RED).append('X').append(RESET).append(' ');
                        case "PENDIENTE" -> line.append(YELLOW).append('*').append(RESET).append(' ');
                        default -> line.append(WHITE).append('.').append(RESET).append(' ');
                    }
                }
            }

            lines.add(line.toString());
        }

        return lines;
    }

    private static String headerRow(boolean compact) {
        StringBuilder sb = new StringBuilder(compact ? "  " : "   ");
        for (int c = 0; c < Board.SIZE; c++) {
            sb.append(c).append(' ');
        }
        return sb.toString();
    }

    private static boolean hasShipAt(Board board, int row, int col) {
        for (Ship ship : board.getShips()) {
            if (ship.occupies(new Coordinate(row, col))) {
                return true;
            }
        }
        return false;
    }

    private static boolean containsCoordinate(List<Coordinate> coordinates, int row, int col) {
        for (Coordinate coordinate : coordinates) {
            if (coordinate.getRow() == row && coordinate.getColumn() == col) {
                return true;
            }
        }
        return false;
    }

    private static String key(int row, int col) {
        return row + "," + col;
    }

    private static String safe(String text) {
        return text == null || text.isBlank() ? "-" : text;
    }
}