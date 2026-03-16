package battleship.server;

import battleship.model.*;
import battleship.remote.BattleshipServer;
import battleship.remote.ClientCallback;


import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class BattleshipServerImpl extends UnicastRemoteObject implements BattleshipServer {

    private final Map<String, UserSession> users = new ConcurrentHashMap<>();
    private final Map<String, Room> rooms = new ConcurrentHashMap<>();

    public BattleshipServerImpl() throws RemoteException {
        super();
    }

    @Override
    public synchronized boolean registerPlayer(String username, ClientCallback callback) throws RemoteException {
        if (users.containsKey(username)) return false;

        users.put(username, new UserSession(username, callback));
        sendLog("Usuario " + username + " conectado -> Ok");
        return true;
    }

    @Override
    public synchronized boolean newRoom(String username, String roomName, int maxPlayers) throws RemoteException {
        if (!users.containsKey(username)) {
            sendLog("NewRoom " + roomName + " " + maxPlayers + " -> Error usuario no registrado");
            return false;
        }

        if (roomName == null || roomName.isBlank()) {
            sendLog("NewRoom " + roomName + " " + maxPlayers + " -> Error nombre inválido");
            return false;
        }

        if (maxPlayers < 2 || maxPlayers > 4) {
            sendLog("NewRoom " + roomName + " " + maxPlayers + " -> Error número jugadores inválido");
            return false;
        }

        UserSession session = users.get(username);
        if (session.isInRoom()) {
            sendLog("NewRoom " + roomName + " " + maxPlayers + " -> Error usuario ya está en una sala");
            return false;
        }

        if (rooms.containsKey(roomName)) {
            sendLog("NewRoom " + roomName + " " + maxPlayers + " -> Error la sala ya existe");
            return false;
        }

        Room room = new Room(roomName, maxPlayers);
        room.addUser(username, RoomRole.PLAYER);
        rooms.put(roomName, room);
        session.setRoomName(roomName);

        session.getCallback().notificar("Sala creada: " + roomName);
        sendLog("NewRoom " + roomName + " " + maxPlayers + " -> Ok");

        checkStartGame(room);
        return true;
    }

    @Override
    public synchronized boolean joinRoom(String username, String roomName, String role) throws RemoteException {
        UserSession session = users.get(username);
        Room room = rooms.get(roomName);

        if (session == null) {
            sendLog("JoinRoom " + roomName + " -> Error usuario no registrado");
            return false;
        }

        if (session.isInRoom()) {
            sendLog("JoinRoom " + roomName + " -> Error usuario ya está en una sala");
            return false;
        }

        if (room == null) {
            sendLog("JoinRoom " + roomName + " -> Error no existe la sala");
            return false;
        }

        RoomRole roomRole;
        try {
            roomRole = RoomRole.valueOf(role.toUpperCase());
        } catch (Exception e) {
            sendLog("JoinRoom " + roomName + " -> Error rol inválido");
            return false;
        }

        boolean joined = room.addUser(username, roomRole);
        if (!joined) {
            sendLog("JoinRoom " + roomName + " -> Error sala llena para jugadores");
            return false;
        }

        session.setRoomName(roomName);
        session.getCallback().notificar("Conectado a sala: " + roomName + " como " + roomRole);
        sendLog("JoinRoom " + roomName + " -> Ok");

        checkStartGame(room);
        return true;
    }

    @Override
    public synchronized boolean leaveRoom(String username) throws RemoteException {
        UserSession session = users.get(username);
        if (session == null || !session.isInRoom()) {
            sendLog("LeaveRoom -> Error usuario no está en una sala");
            return false;
        }

        String roomName = session.getRoomName();
        Room room = rooms.get(roomName);

        if (room == null) {
            session.setRoomName(null);
            sendLog("LeaveRoom -> Error sala inexistente");
            return false;
        }

        RoomRole role = room.getUsers().get(username);
        room.removeUser(username);
        session.setRoomName(null);
        if (role == RoomRole.PLAYER) {
            finalizeGameIfNeeded(room);
        }

        if (room.isEmpty()) {
            rooms.remove(roomName);
            sendLog("LeaveRoom -> Ok, sala eliminada: " + roomName);
        } else {
            if (role != null) {
                notifyRoom(room, "El usuario " + username + " ha abandonado la sala (" + role + ").");
            } else {
                notifyRoom(room, "El usuario " + username + " ha abandonado la sala.");
            }
            sendLog("LeaveRoom -> Ok");
        }

        return true;
    }

    @Override
    public synchronized boolean exit(String username) throws RemoteException {
        UserSession session = users.get(username);
        if (session == null) {
            sendLog("Exit -> Error usuario no registrado");
            return false;
        }

        if (session.isInRoom()) {
            leaveRoom(username);
        }

        users.remove(username);
        sendLog("Exit -> Ok");
        return true;
    }

    private void checkStartGame(Room room) throws RemoteException {
        if (room.getPhase() == GamePhase.WAITING_PLAYERS && room.isFullForPlayers()) {
            room.setPhase(GamePhase.PLACING_SHIPS);
            notifyRoom(room, "La partida va a comenzar. Todos los jugadores deben colocar sus barcos.");
            sendLog("Sala " + room.getName() + " completa -> partida en fase PLACING_SHIPS");
        }
    }

    private void notifyRoom(Room room, String message) {
        for (String username : room.getUsers().keySet()) {
            UserSession session = users.get(username);
            if (session != null) {
                try {
                    session.getCallback().notificar(message);
                } catch (Exception ignored) {
                }
            }
        }
    }

    private void sendLog(String message) {
        for (UserSession session : users.values()) {
            if (!session.isInRoom()) continue;

            String roomName = session.getRoomName();
            Room room = rooms.get(roomName);
            if (room == null) continue;

            RoomRole role = room.getUsers().get(session.getUsername());
            if (role == RoomRole.ADMIN) {
                try {
                    session.getCallback().notificar("[Admin:Logs] " + message);
                } catch (Exception ignored) {
                }
            }
        }
    }

    @Override
    public synchronized boolean playerReady(String username) throws RemoteException {
        UserSession session = users.get(username);
        if (session == null || !session.isInRoom()) {
            sendLog("PlayerReady -> Error usuario no está en una sala");
            return false;
        }

        Room room = rooms.get(session.getRoomName());
        if (room == null) {
            sendLog("PlayerReady -> Error sala inexistente");
            return false;
        }

        boolean ready = room.markPlayerReady(username);
        if (!ready) {
            sendLog("PlayerReady " + username + " -> Error no permitido en esta fase o rol");
            return false;
        }

        notifyRoom(room, "El jugador " + username + " ya ha colocado sus barcos.");
        sendLog("PlayerReady " + username + " -> Ok");

        if (room.areAllPlayersReady()) {
            room.setPhase(GamePhase.PLAYING);
            notifyRoom(room, "Todos los jugadores han colocado sus barcos. La partida empieza.");
            sendLog("Sala " + room.getName() + " -> fase PLAYING");
        }

        return true;
    }

    @Override
    public synchronized boolean submitShot(String username, int row, int column) throws RemoteException {
        UserSession session = users.get(username);
        if (session == null || !session.isInRoom()) {
            sendLog("SubmitShot -> Error usuario no está en una sala");
            return false;
        }

        Room room = rooms.get(session.getRoomName());
        if (room == null) {
            sendLog("SubmitShot -> Error sala inexistente");
            return false;
        }

        boolean submitted = room.submitShot(username, new Coordinate(row, column));
        if (!submitted) {
            sendLog("SubmitShot " + username + " -> Error no permitido en esta fase, rol o turno");
            return false;
        }

        notifyRoom(room, "El jugador " + username + " ha enviado su disparo.");
        sendLog("SubmitShot " + username + " (" + row + "," + column + ") -> Ok");

        if (room.haveAllAlivePlayersSubmittedShot()) {
            notifyRoom(room, "Todos los jugadores han enviado su disparo. Resolviendo turno...");
            sendLog("Sala " + room.getName() + " -> todos los disparos del turno recibidos");
            resolveTurn(room);
        }

        return true;
    }
    private void finalizeGameIfNeeded(Room room) throws RemoteException {
        if (room.getAlivePlayers().size() > 1) {
            return;
        }

        room.setPhase(GamePhase.FINISHED);

        String winner = null;
        for (String survivor : room.getAlivePlayers()) {
            winner = survivor;
            break;
        }

        if (winner != null) {
            notifyRoom(room, "Partida terminada. Ganador: " + winner);
            sendLog("Sala " + room.getName() + " -> FINISHED. Ganador: " + winner);
        } else {
            notifyRoom(room, "Partida terminada sin ganador.");
            sendLog("Sala " + room.getName() + " -> FINISHED sin ganador");
        }
    }

    private void resolveTurn(Room room) throws RemoteException {
        Map<String, Coordinate> shots = new HashMap<>(room.getCurrentTurnShots());
        HashSet<String> aliveAtStart = new HashSet<>(room.getAlivePlayers());

        Map<String, ResultantShot> globalResults = new HashMap<>();
        Map<String, List<String>> details = new HashMap<>();

        for (Map.Entry<String, Coordinate> shotEntry : shots.entrySet()) {
            String shooter = shotEntry.getKey();
            Coordinate shot = shotEntry.getValue();

            ResultantShot globalResult = ResultantShot.MISS;
            List<String> shotDetails = new ArrayList<>();

            for (String target : aliveAtStart) {
                if (target.equals(shooter)) continue;

                UserSession targetSession = users.get(target);
                if (targetSession == null) continue;

                ResultantShot localResult = targetSession.getCallback()
                        .resolveIncomingShot(shot.getRow(), shot.getColumn());

                shotDetails.add("contra " + target + ": " + localResult);

                if (localResult == ResultantShot.SUNK) {
                    globalResult = ResultantShot.SUNK;
                } else if (localResult == ResultantShot.HIT && globalResult != ResultantShot.SUNK) {
                    globalResult = ResultantShot.HIT;
                }
            }

            globalResults.put(shooter, globalResult);
            details.put(shooter, shotDetails);
        }

        HashSet<String> defeatedPlayers = new HashSet<>();

        for (String target : aliveAtStart) {
            UserSession targetSession = users.get(target);
            if (targetSession == null) continue;

            if (targetSession.getCallback().hasLost()) {
                defeatedPlayers.add(target);
            }
        }

        for (String defeated : defeatedPlayers) {
            room.convertPlayerToSpectator(defeated);
            notifyRoom(room, "El jugador " + defeated + " ha perdido todos sus barcos y pasa a SPECTATOR.");
            sendLog("Sala " + room.getName() + " -> jugador derrotado: " + defeated);
        }

        for (Map.Entry<String, Coordinate> shotEntry : shots.entrySet()) {
            String shooter = shotEntry.getKey();
            Coordinate shot = shotEntry.getValue();
            ResultantShot result = globalResults.get(shooter);
            List<String> shotDetails = details.get(shooter);

            StringBuilder sb = new StringBuilder();
            sb.append("Resultado del disparo de ")
                    .append(shooter)
                    .append(" en (")
                    .append(shot.getRow())
                    .append(",")
                    .append(shot.getColumn())
                    .append("): ")
                    .append(result);

            if ((shotDetails != null) && !shotDetails.isEmpty()) {
                sb.append(" -> ").append(String.join(" | ", shotDetails));
            }

            notifyRoom(room, sb.toString());
        }

        room.clearTurnShots();

        finalizeGameIfNeeded(room);
        if (room.getPhase() == GamePhase.FINISHED) {
            return;
        }

        notifyRoom(room, "Turno resuelto. Comienza un nuevo turno.");
        sendLog("Sala " + room.getName() + " -> turno resuelto, nuevo turno iniciado");
    }


}
