package battleship.model;

import java.io.Serializable;

public class Game implements Serializable {
    private final String player1;
    private final String player2;
    private final Board board1 = new Board();
    private final Board board2 = new Board();

    private String currentTurn;
    private boolean started = false;
    private boolean finished = false;
    private String winner;

    public Game(String player1, String player2) {
        this.player1 = player1;
        this.player2 = player2;
        this.currentTurn = player1;
    }

    public Board getBoardOf(String jugador) {
        if (player1.equals(jugador)) return board1;
        if (player2.equals(jugador)) return board2;
        throw new IllegalArgumentException("Jugador no pertenece a la partida");
    }

    public Board getOpponentBoardOf(String jugador) {
        if (player1.equals(jugador)) return board2;
        if (player2.equals(jugador)) return board1;
        throw new IllegalArgumentException("Jugador no pertenece a la partida");
    }

    public String getOpponentOf(String jugador) {
        if (player1.equals(jugador)) return player2;
        if (player2.equals(jugador)) return player1;
        throw new IllegalArgumentException("Jugador no pertenece a la partida");
    }

    public String getCurrentTurn() {
        return currentTurn;
    }

    public void setStarted(boolean started) {
        this.started = started;
    }

    public boolean isStarted() {
        return started;
    }

    public boolean isFinished() {
        return finished;
    }

    public String getWinner() {
        return winner;
    }

    public ResultantShot shoot(String player, Coordinate c) {
        if (!started) {
            throw new IllegalStateException("La partida no ha comenzado");
        }

        if (finished) {
            throw new IllegalStateException("La partida ya terminó");
        }

        if (!currentTurn.equals(player)) {
            throw new IllegalStateException("No es tu turno");
        }

        Board rival = getOpponentBoardOf(player);
        ResultantShot result = rival.shoot(c);

        if (rival.allSunk()) {
            finished = true;
            winner = player;
            return result;
        }

        if (result != ResultantShot.REPEATED) {
            currentTurn = getOpponentOf(player);
        }

        return result;
    }
}