package src.battleship.server;

import battleship.model.*;
import battleship.server.BattleshipServerImpl;
import org.junit.jupiter.api.Test;
import src.battleship.testdoubles.ClientCallStub;

import static org.junit.jupiter.api.Assertions.*;

public class BattleshipServerImplTest {

    @Test
    void registerPlayerReturnsTrueFirstTimeAndFalseIfDuplicated() throws Exception {
        BattleshipServerImpl server = new BattleshipServerImpl();
        ClientCallStub callback1 = new ClientCallStub();
        ClientCallStub callback2 = new ClientCallStub();

        assertTrue(server.registerPlayer("Alice", callback1));
        assertFalse(server.registerPlayer("Alice", callback2));
    }

    @Test
    void newRoomWorksForRegisteredUser() throws Exception {
        BattleshipServerImpl server = new BattleshipServerImpl();
        ClientCallStub callback = new ClientCallStub();

        server.registerPlayer("Alice", callback);

        boolean created = server.newRoom("Alice", "Sala1", 2);

        assertTrue(created);
        assertTrue(callback.containsMessage("Sala creada: Sala1"));
    }

    @Test
    void newRoomFailsIfUserIsNotRegistered() throws Exception {
        BattleshipServerImpl server = new BattleshipServerImpl();

        boolean created = server.newRoom("Alice", "Sala1", 2);

        assertFalse(created);
    }

    @Test
    void newRoomFailsWithInvalidPlayerCount() throws Exception {
        BattleshipServerImpl server = new BattleshipServerImpl();
        ClientCallStub callback = new ClientCallStub();

        server.registerPlayer("Alice", callback);

        assertFalse(server.newRoom("Alice", "Sala1", 1));
        assertFalse(server.newRoom("Alice", "Sala1", 5));
    }

    @Test
    void newRoomFailsIfRoomAlreadyExists() throws Exception {
        BattleshipServerImpl server = new BattleshipServerImpl();
        ClientCallStub aliceCallback = new ClientCallStub();
        ClientCallStub bobCallback = new ClientCallStub();

        server.registerPlayer("Alice", aliceCallback);
        server.registerPlayer("Bob", bobCallback);

        assertTrue(server.newRoom("Alice", "Sala1", 2));
        assertFalse(server.newRoom("Bob", "Sala1", 2));
    }

    @Test
    void newRoomFailsIfUserIsAlreadyInsideAnotherRoom() throws Exception {
        BattleshipServerImpl server = new BattleshipServerImpl();
        ClientCallStub callback = new ClientCallStub();

        server.registerPlayer("Alice", callback);

        assertTrue(server.newRoom("Alice", "Sala1", 2));
        assertFalse(server.newRoom("Alice", "Sala2", 2));
    }

    @Test
    void joinRoomAsPlayerWorksCorrectly() throws Exception {
        BattleshipServerImpl server = new BattleshipServerImpl();
        ClientCallStub aliceCallback = new ClientCallStub();
        ClientCallStub bobCallback = new ClientCallStub();

        server.registerPlayer("Alice", aliceCallback);
        server.registerPlayer("Bob", bobCallback);

        server.newRoom("Alice", "Sala1", 2);
        boolean joined = server.joinRoom("Bob", "Sala1", "PLAYER");

        assertTrue(joined);
        assertTrue(bobCallback.containsMessage("Conectado a sala: Sala1 como PLAYER"));
    }

    @Test
    void joinRoomAsSpectatorWorksCorrectly() throws Exception {
        BattleshipServerImpl server = new BattleshipServerImpl();
        ClientCallStub aliceCallback = new ClientCallStub();
        ClientCallStub bobCallback = new ClientCallStub();

        server.registerPlayer("Alice", aliceCallback);
        server.registerPlayer("Bob", bobCallback);

        server.newRoom("Alice", "Sala1", 2);
        boolean joined = server.joinRoom("Bob", "Sala1", "SPECTATOR");

        assertTrue(joined);
        assertTrue(bobCallback.containsMessage("Conectado a sala: Sala1 como SPECTATOR"));
    }

    @Test
    void joinRoomFailsWithInvalidRole() throws Exception {
        BattleshipServerImpl server = new BattleshipServerImpl();
        ClientCallStub aliceCallback = new ClientCallStub();
        ClientCallStub bobCallback = new ClientCallStub();

        server.registerPlayer("Alice", aliceCallback);
        server.registerPlayer("Bob", bobCallback);

        server.newRoom("Alice", "Sala1", 2);
        boolean joined = server.joinRoom("Bob", "Sala1", "BANANA");

        assertFalse(joined);
    }

    @Test
    void joinRoomFailsIfRoomDoesNotExist() throws Exception {
        BattleshipServerImpl server = new BattleshipServerImpl();
        ClientCallStub callback = new ClientCallStub();

        server.registerPlayer("Bob", callback);

        boolean joined = server.joinRoom("Bob", "SalaFantasma", "PLAYER");

        assertFalse(joined);
    }

    @Test
    void joinRoomFailsIfUserIsAlreadyInRoom() throws Exception {
        BattleshipServerImpl server = new BattleshipServerImpl();
        ClientCallStub aliceCallback = new ClientCallStub();
        ClientCallStub bobCallback = new ClientCallStub();

        server.registerPlayer("Alice", aliceCallback);
        server.registerPlayer("Bob", bobCallback);

        server.newRoom("Alice", "Sala1", 2);
        server.joinRoom("Bob", "Sala1", "SPECTATOR");

        boolean joinedAgain = server.joinRoom("Bob", "Sala1", "PLAYER");

        assertFalse(joinedAgain);
    }

    @Test
    void joinRoomFailsForThirdPlayerWhenRoomMaxPlayersIsTwo() throws Exception {
        BattleshipServerImpl server = new BattleshipServerImpl();
        ClientCallStub aliceCallback = new ClientCallStub();
        ClientCallStub bobCallback = new ClientCallStub();
        ClientCallStub carolCallback = new ClientCallStub();

        server.registerPlayer("Alice", aliceCallback);
        server.registerPlayer("Bob", bobCallback);
        server.registerPlayer("Carol", carolCallback);

        server.newRoom("Alice", "Sala1", 2);
        assertTrue(server.joinRoom("Bob", "Sala1", "PLAYER"));

        boolean thirdPlayerJoined = server.joinRoom("Carol", "Sala1", "PLAYER");

        assertFalse(thirdPlayerJoined);
    }

    @Test
    void roomPhaseStartsWhenPlayerSlotsAreFilled() throws Exception {
        BattleshipServerImpl server = new BattleshipServerImpl();
        ClientCallStub aliceCallback = new ClientCallStub();
        ClientCallStub bobCallback = new ClientCallStub();

        server.registerPlayer("Alice", aliceCallback);
        server.registerPlayer("Bob", bobCallback);

        server.newRoom("Alice", "Sala1", 2);
        server.joinRoom("Bob", "Sala1", "PLAYER");

        assertTrue(aliceCallback.containsMessage("La partida va a comenzar"));
        assertTrue(bobCallback.containsMessage("La partida va a comenzar"));
    }

    @Test
    void adminReceivesLogsWhenActionsOccurInsideRoom() throws Exception {
        BattleshipServerImpl server = new BattleshipServerImpl();
        ClientCallStub aliceCallback = new ClientCallStub();
        ClientCallStub bobCallback = new ClientCallStub();
        ClientCallStub adminCallback = new ClientCallStub();

        server.registerPlayer("Alice", aliceCallback);
        server.registerPlayer("Bob", bobCallback);
        server.registerPlayer("Carol", adminCallback);

        server.newRoom("Alice", "Sala1", 2);
        assertTrue(server.joinRoom("Carol", "Sala1", "ADMIN"));

        adminCallback.clearMessages();

        server.joinRoom("Bob", "Sala1", "PLAYER");

        assertTrue(
                adminCallback.containsMessage("[Admin:Logs] JoinRoom Sala1 -> Ok")
                        || adminCallback.containsMessage("[Admin:Logs] Sala Sala1 completa")
        );
    }

    @Test
    void leaveRoomWorksCorrectly() throws Exception {
        BattleshipServerImpl server = new BattleshipServerImpl();
        ClientCallStub callback = new ClientCallStub();

        server.registerPlayer("Alice", callback);
        server.newRoom("Alice", "Sala1", 2);

        assertTrue(server.leaveRoom("Alice"));
        assertFalse(server.leaveRoom("Alice"));
    }

    @Test
    void emptyRoomGetsDeletedAndCanBeRecreated() throws Exception {
        BattleshipServerImpl server = new BattleshipServerImpl();
        ClientCallStub aliceCallback = new ClientCallStub();
        ClientCallStub bobCallback = new ClientCallStub();

        server.registerPlayer("Alice", aliceCallback);
        server.registerPlayer("Bob", bobCallback);

        assertTrue(server.newRoom("Alice", "Sala1", 2));
        assertTrue(server.leaveRoom("Alice"));

        assertTrue(server.newRoom("Bob", "Sala1", 2));
    }

    @Test
    void exitWorksAndSecondExitFails() throws Exception {
        BattleshipServerImpl server = new BattleshipServerImpl();
        ClientCallStub callback = new ClientCallStub();

        server.registerPlayer("Alice", callback);
        server.newRoom("Alice", "Sala1", 2);

        assertTrue(server.exit("Alice"));
        assertFalse(server.exit("Alice"));
    }

    @Test
    void exitAlsoRemovesUserFromRoomSoRoomCanBeReused() throws Exception {
        BattleshipServerImpl server = new BattleshipServerImpl();
        ClientCallStub aliceCallback = new ClientCallStub();
        ClientCallStub bobCallback = new ClientCallStub();

        server.registerPlayer("Alice", aliceCallback);
        server.registerPlayer("Bob", bobCallback);

        assertTrue(server.newRoom("Alice", "Sala1", 2));
        assertTrue(server.exit("Alice"));

        assertTrue(server.newRoom("Bob", "Sala1", 2));
    }

    @Test
    void playerReadyFailsIfUserIsNotRegistered() throws Exception {
        BattleshipServerImpl server = new BattleshipServerImpl();

        assertFalse(server.playerReady("Ghost"));
    }

    @Test
    void playerReadyFailsIfUserIsNotInsideRoom() throws Exception {
        BattleshipServerImpl server = new BattleshipServerImpl();
        ClientCallStub callback = new ClientCallStub();

        server.registerPlayer("Alice", callback);

        assertFalse(server.playerReady("Alice"));
    }

    @Test
    void playerReadyFailsIfRoomIsNotInPlacingShipsPhase() throws Exception {
        BattleshipServerImpl server = new BattleshipServerImpl();
        ClientCallStub callback = new ClientCallStub();

        server.registerPlayer("Alice", callback);
        server.newRoom("Alice", "Sala1", 2);

        assertFalse(server.playerReady("Alice"));
    }

    @Test
    void playerReadyWorksWhenPlayerIsInsideRoomAndPhaseIsPlacingShips() throws Exception {
        BattleshipServerImpl server = new BattleshipServerImpl();
        ClientCallStub aliceCallback = new ClientCallStub();
        ClientCallStub bobCallback = new ClientCallStub();

        server.registerPlayer("Alice", aliceCallback);
        server.registerPlayer("Bob", bobCallback);

        server.newRoom("Alice", "Sala1", 2);
        server.joinRoom("Bob", "Sala1", "PLAYER");

        boolean ready = server.playerReady("Alice");

        assertTrue(ready);
        assertTrue(aliceCallback.containsMessage("El jugador Alice ya ha colocado sus barcos."));
        assertTrue(bobCallback.containsMessage("El jugador Alice ya ha colocado sus barcos."));
    }

    @Test
    void playerReadyFailsForSpectator() throws Exception {
        BattleshipServerImpl server = new BattleshipServerImpl();
        ClientCallStub aliceCallback = new ClientCallStub();
        ClientCallStub bobCallback = new ClientCallStub();

        server.registerPlayer("Alice", aliceCallback);
        server.registerPlayer("Bob", bobCallback);

        server.newRoom("Alice", "Sala1", 2);
        server.joinRoom("Bob", "Sala1", "SPECTATOR");

        assertFalse(server.playerReady("Bob"));
    }

    @Test
    void whenAllPlayersAreReadyRoomTransitionsToPlaying() throws Exception {
        BattleshipServerImpl server = new BattleshipServerImpl();
        ClientCallStub aliceCallback = new ClientCallStub();
        ClientCallStub bobCallback = new ClientCallStub();

        server.registerPlayer("Alice", aliceCallback);
        server.registerPlayer("Bob", bobCallback);

        server.newRoom("Alice", "Sala1", 2);
        server.joinRoom("Bob", "Sala1", "PLAYER");

        aliceCallback.clearMessages();
        bobCallback.clearMessages();

        assertTrue(server.playerReady("Alice"));
        assertTrue(server.playerReady("Bob"));

        assertTrue(aliceCallback.containsMessage("Todos los jugadores han colocado sus barcos. La partida empieza."));
        assertTrue(bobCallback.containsMessage("Todos los jugadores han colocado sus barcos. La partida empieza."));
    }

    @Test
    void playerReadyCanBeCalledByBothPlayersInOrder() throws Exception {
        BattleshipServerImpl server = new BattleshipServerImpl();
        ClientCallStub aliceCallback = new ClientCallStub();
        ClientCallStub bobCallback = new ClientCallStub();

        server.registerPlayer("Alice", aliceCallback);
        server.registerPlayer("Bob", bobCallback);

        server.newRoom("Alice", "Sala1", 2);
        server.joinRoom("Bob", "Sala1", "PLAYER");

        assertTrue(server.playerReady("Bob"));
        assertTrue(server.playerReady("Alice"));

        assertTrue(aliceCallback.containsMessage("La partida empieza."));
        assertTrue(bobCallback.containsMessage("La partida empieza."));
    }

    @Test
    void submitShotFailsIfPhaseIsNotPlaying() {
        Room room = new Room("Sala1", 2);
        room.addUser("Alice", RoomRole.PLAYER);

        boolean submitted = room.submitShot("Alice", new Coordinate(1, 1));

        assertFalse(submitted);
    }

    @Test
    void submitShotFailsForSpectator() {
        Room room = new Room("Sala1", 2);
        room.addUser("Bob", RoomRole.SPECTATOR);
        room.setPhase(GamePhase.PLAYING);

        boolean submitted = room.submitShot("Bob", new Coordinate(1, 1));

        assertFalse(submitted);
    }

    @Test
    void submitShotWorksForAlivePlayerDuringPlaying() {
        Room room = new Room("Sala1", 2);
        room.addUser("Alice", RoomRole.PLAYER);
        room.setPhase(GamePhase.PLAYING);

        boolean submitted = room.submitShot("Alice", new Coordinate(2, 3));

        assertTrue(submitted);
        assertEquals(1, room.getCurrentTurnShots().size());
        assertEquals(2, room.getCurrentTurnShots().get("Alice").getRow());
        assertEquals(3, room.getCurrentTurnShots().get("Alice").getColumn());
    }

    @Test
    void submitShotFailsIfPlayerAlreadySubmittedThisTurn() {
        Room room = new Room("Sala1", 2);
        room.addUser("Alice", RoomRole.PLAYER);
        room.setPhase(GamePhase.PLAYING);

        assertTrue(room.submitShot("Alice", new Coordinate(2, 3)));
        assertFalse(room.submitShot("Alice", new Coordinate(4, 5)));
    }

    @Test
    void haveAllAlivePlayersSubmittedShotReturnsTrueWhenAllShotsArePresent() {
        Room room = new Room("Sala1", 2);
        room.addUser("Alice", RoomRole.PLAYER);
        room.addUser("Bob", RoomRole.PLAYER);
        room.setPhase(GamePhase.PLAYING);

        room.submitShot("Alice", new Coordinate(1, 1));
        room.submitShot("Bob", new Coordinate(2, 2));

        assertTrue(room.haveAllAlivePlayersSubmittedShot());
    }

    @Test
    void clearTurnShotsEmptiesCurrentTurnShots() {
        Room room = new Room("Sala1", 2);
        room.addUser("Alice", RoomRole.PLAYER);
        room.setPhase(GamePhase.PLAYING);

        room.submitShot("Alice", new Coordinate(1, 1));
        assertFalse(room.getCurrentTurnShots().isEmpty());

        room.clearTurnShots();

        assertTrue(room.getCurrentTurnShots().isEmpty());
    }

    @Test
    void whenAllPlayersSubmitShotsTurnGetsResolvedAndPlayersReceiveResultMessages() throws Exception {
        BattleshipServerImpl server = new BattleshipServerImpl();
        ClientCallStub alice = new ClientCallStub();
        ClientCallStub bob = new ClientCallStub();

        server.registerPlayer("Alice", alice);
        server.registerPlayer("Bob", bob);

        server.newRoom("Alice", "Sala1", 2);
        server.joinRoom("Bob", "Sala1", "PLAYER");

        server.playerReady("Alice");
        server.playerReady("Bob");

        alice.clearMessages();
        bob.clearMessages();

        bob.enqueueShotResult(battleship.model.ResultantShot.HIT);
        alice.enqueueShotResult(battleship.model.ResultantShot.MISS);

        assertTrue(server.submitShot("Alice", 1, 1));
        assertTrue(server.submitShot("Bob", 2, 2));

        assertTrue(alice.containsMessage("Resultado del disparo de Alice"));
        assertTrue(alice.containsMessage("Resultado del disparo de Bob"));
        assertTrue(bob.containsMessage("Resultado del disparo de Alice"));
        assertTrue(bob.containsMessage("Resultado del disparo de Bob"));
        assertTrue(alice.containsMessage("Turno resuelto"));
        assertTrue(bob.containsMessage("Turno resuelto"));
    }

    @Test
    void defeatedPlayerBecomesSpectatorAndGameFinishesIfOnlyOnePlayerRemains() throws Exception {
        BattleshipServerImpl server = new BattleshipServerImpl();
        ClientCallStub alice = new ClientCallStub();
        ClientCallStub bob = new ClientCallStub();

        server.registerPlayer("Alice", alice);
        server.registerPlayer("Bob", bob);

        server.newRoom("Alice", "Sala1", 2);
        server.joinRoom("Bob", "Sala1", "PLAYER");

        server.playerReady("Alice");
        server.playerReady("Bob");

        alice.clearMessages();
        bob.clearMessages();

        bob.enqueueShotResult(battleship.model.ResultantShot.SUNK);
        bob.setLost(true);

        alice.enqueueShotResult(battleship.model.ResultantShot.MISS);

        assertTrue(server.submitShot("Alice", 3, 3));
        assertTrue(server.submitShot("Bob", 4, 4));

        assertTrue(alice.containsMessage("pasa a SPECTATOR"));
        assertTrue(bob.containsMessage("pasa a SPECTATOR"));
        assertTrue(alice.containsMessage("Partida terminada. Ganador: Alice"));
        assertTrue(bob.containsMessage("Partida terminada. Ganador: Alice"));
    }
}