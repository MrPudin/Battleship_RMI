package battleship.server;

import battleship.dto.ShipDTO;
import battleship.model.*;
import battleship.remote.BattleshipServer;
import battleship.remote.ClientCallback;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;

public class BattleshipServerImpl extends UnicastRemoteObject implements BattleshipServer {

    private final Map<String, ClientCallback> callbacks = new HashMap<>();
    private final Map<String, Game> gamePerPlayer = new HashMap<>();

    public BattleshipServerImpl() throws RemoteException {
        super();
    }

    @Override
    public synchronized boolean registerPlayer(String name, ClientCallback callback) {
        if (callbacks.containsKey(name)) return false;
        callbacks.put(name, callback);
        return true;
    }

    @Override
    public synchronized boolean createGame(String player1, String player2) throws RemoteException {
        if (!callbacks.containsKey(player1) || !callbacks.containsKey(player2)) return false;
        if (gamePerPlayer.containsKey(player1) || gamePerPlayer.containsKey(player2)) return false;

        Game game = new Game(player1, player2);
        gamePerPlayer.put(player1, game);
        gamePerPlayer.put(player2, game);

        callbacks.get(player1).notificar("Partida creada contra " + player2);
        callbacks.get(player2).notificar("Partida creada contra " + player1);
        return true;
    }

    @Override
    public synchronized boolean putShips(String player, List<ShipDTO> ShipsDto) throws RemoteException {
        Game game = gamePerPlayer.get(player);
        if (game == null) return false;

        Board tablero = new Board();
        for (ShipDTO dto : ShipsDto) {
            ShipType tipo = ShipType.valueOf(dto.type);
            Orientation orientation = Orientation.valueOf(dto.orientation);
            Ship barco = new Ship(tipo, new Coordinate(dto.row, dto.column), orientation);
            if (!tablero.colocarBarco(barco)) return false;
        }

        Board real = game.getTableroDe(player);
        real.getShips().clear();
        real.getShips().addAll(tablero.getShips());

        String rival = game.getRivalDe(player);
        if (!game.getTableroDe(player).getShips().isEmpty() &&
                !game.getTableroDe(rival).getShips().isEmpty()) {
            game.setStarted(true);
            callbacks.get(player).notificar("La partida ha comenzado");
            callbacks.get(rival).notificar("La partida ha comenzado");
        }

        return true;
    }

    @Override
    public synchronized ResultantShot shot(String player, int row, int column) throws RemoteException {
        Game game = gamePerPlayer.get(player);
        if (game == null) throw new RemoteException("Jugador sin partida");
        ResultantShot resultado = game.disparar(player, new Coordinate(row, column));

        String rival = game.getRivalDe(player);
        callbacks.get(player).notificar("Resultado disparo: " + resultado);
        callbacks.get(rival).notificar("El rival ha disparado en (" + row + "," + column + ")");

        if (game.isFinished()) {
            callbacks.get(player).notificar("Has ganado");
            callbacks.get(rival).notificar("Has perdido");
        }

        return resultado;
    }

    @Override
    public synchronized String obtainActualTurn(String player) {
        Game game = gamePerPlayer.get(player);
        if (game == null) return null;
        return game.getCurrentTurn();
    }

    @Override
    public synchronized boolean startedGame(String player) {
        Game game = gamePerPlayer.get(player);
        return game != null && game.isStarted();
    }
}