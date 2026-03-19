package src.battleship.server;

import battleship.model.ResultantShot;
import battleship.model.ShipType;
import battleship.server.BattleshipServerImpl;
import battleship.testdoubles.ClientCallStub;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class BattleshipServerImplTest {

    @Test
    void registerPlayerSucceedsForNewUser() throws Exception {
        BattleshipServerImpl server = new BattleshipServerImpl();
        ClientCallStub alice = new ClientCallStub();

        assertTrue(server.registerPlayer("alice", alice));
    }

    @Test
    void registerPlayerFailsForDuplicateUser() throws Exception {
        BattleshipServerImpl server = new BattleshipServerImpl();
        ClientCallStub alice1 = new ClientCallStub();
        ClientCallStub alice2 = new ClientCallStub();

        assertTrue(server.registerPlayer("alice", alice1));
        assertFalse(server.registerPlayer("alice", alice2));
    }

    @Test
    void registerPlayerFailsForInvalidArguments() throws Exception {
        BattleshipServerImpl server = new BattleshipServerImpl();
        ClientCallStub alice = new ClientCallStub();

        assertFalse(server.registerPlayer(null, alice));
        assertFalse(server.registerPlayer("", alice));
        assertFalse(server.registerPlayer("alice", null));
    }

    @Test
    void newRoomSucceedsForRegisteredUser() throws Exception {
        BattleshipServerImpl server = new BattleshipServerImpl();
        ClientCallStub alice = new ClientCallStub();

        assertTrue(server.registerPlayer("alice", alice));
        assertTrue(server.newRoom("alice", "room1", 2));
        assertTrue(alice.containsMessage("Sala creada: room1"));
    }

    @Test
    void newRoomFailsForDuplicateRoomName() throws Exception {
        BattleshipServerImpl server = new BattleshipServerImpl();
        ClientCallStub alice = new ClientCallStub();
        ClientCallStub bob = new ClientCallStub();

        assertTrue(server.registerPlayer("alice", alice));
        assertTrue(server.registerPlayer("bob", bob));

        assertTrue(server.newRoom("alice", "room1", 2));
        assertFalse(server.newRoom("bob", "room1", 2));
    }

    @Test
    void newRoomFailsForInvalidPlayerCount() throws Exception {
        BattleshipServerImpl server = new BattleshipServerImpl();
        ClientCallStub alice = new ClientCallStub();

        assertTrue(server.registerPlayer("alice", alice));

        assertFalse(server.newRoom("alice", "room1", 1));
        assertFalse(server.newRoom("alice", "room2", 5));
    }

    @Test
    void joinRoomSucceedsForValidPlayerRole() throws Exception {
        BattleshipServerImpl server = new BattleshipServerImpl();
        ClientCallStub alice = new ClientCallStub();
        ClientCallStub bob = new ClientCallStub();

        assertTrue(server.registerPlayer("alice", alice));
        assertTrue(server.registerPlayer("bob", bob));
        assertTrue(server.newRoom("alice", "room1", 2));

        assertTrue(server.joinRoom("bob", "room1", "PLAYER"));
        assertTrue(bob.containsMessage("Conectado a sala: room1 como PLAYER"));
    }

    @Test
    void joinRoomFailsForInvalidRole() throws Exception {
        BattleshipServerImpl server = new BattleshipServerImpl();
        ClientCallStub alice = new ClientCallStub();
        ClientCallStub bob = new ClientCallStub();

        assertTrue(server.registerPlayer("alice", alice));
        assertTrue(server.registerPlayer("bob", bob));
        assertTrue(server.newRoom("alice", "room1", 2));

        assertFalse(server.joinRoom("bob", "room1", "banana"));
    }

    @Test
    void joinRoomFailsIfRoomDoesNotExist() throws Exception {
        BattleshipServerImpl server = new BattleshipServerImpl();
        ClientCallStub bob = new ClientCallStub();

        assertTrue(server.registerPlayer("bob", bob));
        assertFalse(server.joinRoom("bob", "ghost-room", "PLAYER"));
    }

    @Test
    void joinRoomFailsWhenPlayerSlotsAreFull() throws Exception {
        BattleshipServerImpl server = new BattleshipServerImpl();
        ClientCallStub alice = new ClientCallStub();
        ClientCallStub bob = new ClientCallStub();
        ClientCallStub carol = new ClientCallStub();

        assertTrue(server.registerPlayer("alice", alice));
        assertTrue(server.registerPlayer("bob", bob));
        assertTrue(server.registerPlayer("carol", carol));

        assertTrue(server.newRoom("alice", "room1", 2));
        assertTrue(server.joinRoom("bob", "room1", "PLAYER"));
        assertFalse(server.joinRoom("carol", "room1", "PLAYER"));
    }

    @Test
    void joinRoomAllowsSpectatorEvenWhenPlayerSlotsAreFull() throws Exception {
        BattleshipServerImpl server = new BattleshipServerImpl();
        ClientCallStub alice = new ClientCallStub();
        ClientCallStub bob = new ClientCallStub();
        ClientCallStub spectator = new ClientCallStub();

        assertTrue(server.registerPlayer("alice", alice));
        assertTrue(server.registerPlayer("bob", bob));
        assertTrue(server.registerPlayer("spectator", spectator));

        assertTrue(server.newRoom("alice", "room1", 2));
        assertTrue(server.joinRoom("bob", "room1", "PLAYER"));
        assertTrue(server.joinRoom("spectator", "room1", "SPECTATOR"));
    }

    @Test
    void roomEntersPlacingShipsWhenPlayerSlotsAreFilled() throws Exception {
        BattleshipServerImpl server = new BattleshipServerImpl();
        ClientCallStub alice = new ClientCallStub();
        ClientCallStub bob = new ClientCallStub();

        assertTrue(server.registerPlayer("alice", alice));
        assertTrue(server.registerPlayer("bob", bob));

        assertTrue(server.newRoom("alice", "room1", 2));
        assertTrue(server.joinRoom("bob", "room1", "PLAYER"));

        assertTrue(alice.containsMessage("La partida va a comenzar"));
        assertTrue(bob.containsMessage("La partida va a comenzar"));
    }

    @Test
    void playerReadyFailsIfUserNotInRoom() throws Exception {
        BattleshipServerImpl server = new BattleshipServerImpl();
        ClientCallStub alice = new ClientCallStub();

        assertTrue(server.registerPlayer("alice", alice));
        assertFalse(server.playerReady("alice"));
    }

    @Test
    void playerReadyStartsGameWhenAllPlayersReady() throws Exception {
        BattleshipServerImpl server = setupTwoPlayerGame();
        ClientCallStub alice = new ClientCallStub();
        ClientCallStub bob = new ClientCallStub();

        // reconstruimos porque setupTwoPlayerGame crea los suyos
        server = new BattleshipServerImpl();
        alice = new ClientCallStub();
        bob = new ClientCallStub();

        assertTrue(server.registerPlayer("alice", alice));
        assertTrue(server.registerPlayer("bob", bob));
        assertTrue(server.newRoom("alice", "room1", 2));
        assertTrue(server.joinRoom("bob", "room1", "PLAYER"));

        assertTrue(server.playerReady("alice"));
        assertTrue(server.playerReady("bob"));

        assertTrue(alice.containsMessage("La partida empieza"));
        assertTrue(bob.containsMessage("La partida empieza"));
    }

    @Test
    void submitShotFailsIfCoordinateIsOutOfBounds() throws Exception {
        BattleshipServerImpl server = new BattleshipServerImpl();
        ClientCallStub alice = new ClientCallStub();
        ClientCallStub bob = new ClientCallStub();

        assertTrue(server.registerPlayer("alice", alice));
        assertTrue(server.registerPlayer("bob", bob));
        assertTrue(server.newRoom("alice", "room1", 2));
        assertTrue(server.joinRoom("bob", "room1", "PLAYER"));
        assertTrue(server.playerReady("alice"));
        assertTrue(server.playerReady("bob"));

        assertFalse(server.submitShot("alice", -1, 5));
        assertFalse(server.submitShot("alice", 5, 10));
    }

    @Test
    void submitShotFailsIfUserHasAlreadyShotThisTurn() throws Exception {
        BattleshipServerImpl server = new BattleshipServerImpl();
        ClientCallStub alice = new ClientCallStub();
        ClientCallStub bob = new ClientCallStub();

        assertTrue(server.registerPlayer("alice", alice));
        assertTrue(server.registerPlayer("bob", bob));
        assertTrue(server.newRoom("alice", "room1", 2));
        assertTrue(server.joinRoom("bob", "room1", "PLAYER"));
        assertTrue(server.playerReady("alice"));
        assertTrue(server.playerReady("bob"));

        assertTrue(server.submitShot("alice", 1, 1));
        assertFalse(server.submitShot("alice", 2, 2));
    }

    @Test
    void resolvesTurnAndNotifiesBatchedResultsToAllPlayers() throws Exception {
        BattleshipServerImpl server = new BattleshipServerImpl();

        ClientCallStub alice = new ClientCallStub();
        ClientCallStub bob = new ClientCallStub();

        assertTrue(server.registerPlayer("alice", alice));
        assertTrue(server.registerPlayer("bob", bob));

        assertTrue(server.newRoom("alice", "room1", 2));
        assertTrue(server.joinRoom("bob", "room1", "PLAYER"));

        assertTrue(server.playerReady("alice"));
        assertTrue(server.playerReady("bob"));

        alice.enqueueShotResult(ResultantShot.MISS);
        bob.enqueueShotResult(ResultantShot.HIT);

        assertTrue(server.submitShot("alice", 1, 1));
        assertTrue(server.submitShot("bob", 2, 2));

        assertTrue(alice.containsMessage("Resultado del disparo de alice"));
        assertTrue(alice.containsMessage("Resultado del disparo de bob"));
        assertTrue(bob.containsMessage("Resultado del disparo de alice"));
        assertTrue(bob.containsMessage("Resultado del disparo de bob"));
    }

    @Test
    void sinkingShipProducesDetailedSunkMessage() throws Exception {
        BattleshipServerImpl server = new BattleshipServerImpl();

        ClientCallStub alice = new ClientCallStub();
        ClientCallStub bob = new ClientCallStub();

        assertTrue(server.registerPlayer("alice", alice));
        assertTrue(server.registerPlayer("bob", bob));

        assertTrue(server.newRoom("alice", "room1", 2));
        assertTrue(server.joinRoom("bob", "room1", "PLAYER"));

        assertTrue(server.playerReady("alice"));
        assertTrue(server.playerReady("bob"));

        alice.enqueueSunkShotResult(ShipType.BOAT, 2);
        bob.enqueueShotResult(ResultantShot.MISS);

        assertTrue(server.submitShot("alice", 3, 3));
        assertTrue(server.submitShot("bob", 4, 4));

        assertTrue(alice.containsMessage("pierde un BOAT"));
        assertTrue(bob.containsMessage("pierde un BOAT"));
    }

    @Test
    void defeatedPlayerBecomesSpectatorAndGameCanFinish() throws Exception {
        BattleshipServerImpl server = new BattleshipServerImpl();

        ClientCallStub alice = new ClientCallStub();
        ClientCallStub bob = new ClientCallStub();

        assertTrue(server.registerPlayer("alice", alice));
        assertTrue(server.registerPlayer("bob", bob));

        assertTrue(server.newRoom("alice", "room1", 2));
        assertTrue(server.joinRoom("bob", "room1", "PLAYER"));

        assertTrue(server.playerReady("alice"));
        assertTrue(server.playerReady("bob"));

        alice.enqueueShotResult(ResultantShot.MISS);
        bob.enqueueShotResult(ResultantShot.HIT);
        bob.setLost(true);

        assertTrue(server.submitShot("alice", 1, 1));
        assertTrue(server.submitShot("bob", 2, 2));

        assertTrue(alice.containsMessage("pasa a SPECTATOR"));
        assertTrue(bob.containsMessage("pasa a SPECTATOR"));
        assertTrue(alice.containsMessage("Ganador"));
        assertTrue(bob.containsMessage("Ganador"));
    }

    @Test
    void leaveRoomRemovesPlayerAndCanDeleteEmptyRoom() throws Exception {
        BattleshipServerImpl server = new BattleshipServerImpl();
        ClientCallStub alice = new ClientCallStub();

        assertTrue(server.registerPlayer("alice", alice));
        assertTrue(server.newRoom("alice", "room1", 2));
        assertTrue(server.leaveRoom("alice"));

        assertFalse(server.leaveRoom("alice"));
    }

    @Test
    void exitRemovesRegisteredUser() throws Exception {
        BattleshipServerImpl server = new BattleshipServerImpl();
        ClientCallStub alice = new ClientCallStub();

        assertTrue(server.registerPlayer("alice", alice));
        assertTrue(server.exit("alice"));
        assertFalse(server.exit("alice"));
    }

    @Test
    void adminReceivesLogs() throws Exception {
        BattleshipServerImpl server = new BattleshipServerImpl();

        ClientCallStub alice = new ClientCallStub();
        ClientCallStub admin = new ClientCallStub();
        ClientCallStub bob = new ClientCallStub();

        assertTrue(server.registerPlayer("alice", alice));
        assertTrue(server.registerPlayer("admin", admin));
        assertTrue(server.registerPlayer("bob", bob));

        assertTrue(server.newRoom("alice", "room1", 2));
        assertTrue(server.joinRoom("admin", "room1", "ADMIN"));
        assertTrue(server.joinRoom("bob", "room1", "PLAYER"));

        assertTrue(admin.containsMessage("[Admin:Logs]"));
    }

    private BattleshipServerImpl setupTwoPlayerGame() throws Exception {
        BattleshipServerImpl server = new BattleshipServerImpl();
        ClientCallStub alice = new ClientCallStub();
        ClientCallStub bob = new ClientCallStub();

        assertTrue(server.registerPlayer("alice", alice));
        assertTrue(server.registerPlayer("bob", bob));
        assertTrue(server.newRoom("alice", "room1", 2));
        assertTrue(server.joinRoom("bob", "room1", "PLAYER"));

        return server;
    }
}