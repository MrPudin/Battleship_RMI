package battleship.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class RoomTest {

    @Test
    void constructorInitializesRoomCorrectly() {
        Room room = new Room("Sala1", 2);

        assertEquals("Sala1", room.getName());
        assertEquals(2, room.getMaxPlayers());
        assertEquals(GamePhase.WAITING_PLAYERS, room.getPhase());
        assertTrue(room.getUsers().isEmpty());
        assertTrue(room.getAlivePlayers().isEmpty());
        assertTrue(room.isEmpty());
        assertEquals(0, room.countPlayers());
        assertFalse(room.isFullForPlayers());
    }

    @Test
    void addPlayerWorksCorrectly() {
        Room room = new Room("Sala1", 2);

        boolean added = room.addUser("Alice", RoomRole.PLAYER);

        assertTrue(added);
        assertEquals(RoomRole.PLAYER, room.getUsers().get("Alice"));
        assertTrue(room.getAlivePlayers().contains("Alice"));
        assertEquals(1, room.countPlayers());
        assertFalse(room.isEmpty());
    }

    @Test
    void addSpectatorAndAdminDoNotCountAsPlayers() {
        Room room = new Room("Sala1", 2);

        boolean spectatorAdded = room.addUser("Bob", RoomRole.SPECTATOR);
        boolean adminAdded = room.addUser("Carol", RoomRole.ADMIN);

        assertTrue(spectatorAdded);
        assertTrue(adminAdded);

        assertEquals(RoomRole.SPECTATOR, room.getUsers().get("Bob"));
        assertEquals(RoomRole.ADMIN, room.getUsers().get("Carol"));

        assertFalse(room.getAlivePlayers().contains("Bob"));
        assertFalse(room.getAlivePlayers().contains("Carol"));
        assertEquals(0, room.countPlayers());
        assertFalse(room.isFullForPlayers());
    }

    @Test
    void addUserFailsIfUsernameAlreadyExists() {
        Room room = new Room("Sala1", 2);

        boolean firstAdd = room.addUser("Alice", RoomRole.PLAYER);
        boolean secondAdd = room.addUser("Alice", RoomRole.SPECTATOR);

        assertTrue(firstAdd);
        assertFalse(secondAdd);
        assertEquals(1, room.getUsers().size());
        assertEquals(RoomRole.PLAYER, room.getUsers().get("Alice"));
    }

    @Test
    void addPlayerFailsWhenRoomIsFullForPlayers() {
        Room room = new Room("Sala1", 2);

        assertTrue(room.addUser("Alice", RoomRole.PLAYER));
        assertTrue(room.addUser("Bob", RoomRole.PLAYER));
        assertFalse(room.addUser("Carol", RoomRole.PLAYER));

        assertEquals(2, room.countPlayers());
        assertTrue(room.isFullForPlayers());
        assertFalse(room.getUsers().containsKey("Carol"));
    }

    @Test
    void addSpectatorStillWorksWhenPlayerSlotsAreFull() {
        Room room = new Room("Sala1", 2);

        assertTrue(room.addUser("Alice", RoomRole.PLAYER));
        assertTrue(room.addUser("Bob", RoomRole.PLAYER));
        assertTrue(room.addUser("Carol", RoomRole.SPECTATOR));
        assertTrue(room.addUser("Dave", RoomRole.ADMIN));

        assertEquals(4, room.getUsers().size());
        assertEquals(2, room.countPlayers());
        assertTrue(room.isFullForPlayers());
    }

    @Test
    void removeUserRemovesPlayerFromUsersAndAlivePlayers() {
        Room room = new Room("Sala1", 2);

        room.addUser("Alice", RoomRole.PLAYER);
        room.addUser("Bob", RoomRole.SPECTATOR);

        room.removeUser("Alice");

        assertFalse(room.getUsers().containsKey("Alice"));
        assertFalse(room.getAlivePlayers().contains("Alice"));
        assertEquals(0, room.countPlayers());
        assertFalse(room.isEmpty());

        room.removeUser("Bob");
        assertTrue(room.isEmpty());
    }

    @Test
    void removeNonExistingUserDoesNothing() {
        Room room = new Room("Sala1", 2);

        room.addUser("Alice", RoomRole.PLAYER);
        room.removeUser("Ghost");

        assertEquals(1, room.getUsers().size());
        assertTrue(room.getUsers().containsKey("Alice"));
        assertTrue(room.getAlivePlayers().contains("Alice"));
    }

    @Test
    void convertPlayerToSpectatorWorksCorrectly() {
        Room room = new Room("Sala1", 2);

        room.addUser("Alice", RoomRole.PLAYER);
        room.convertPlayerToSpectator("Alice");

        assertEquals(RoomRole.SPECTATOR, room.getUsers().get("Alice"));
        assertFalse(room.getAlivePlayers().contains("Alice"));
        assertEquals(0, room.countPlayers());
    }

    @Test
    void convertNonPlayerToSpectatorDoesNotBreakAnything() {
        Room room = new Room("Sala1", 2);

        room.addUser("Bob", RoomRole.SPECTATOR);
        room.convertPlayerToSpectator("Bob");

        assertEquals(RoomRole.SPECTATOR, room.getUsers().get("Bob"));
        assertFalse(room.getAlivePlayers().contains("Bob"));
        assertEquals(0, room.countPlayers());
    }

    @Test
    void phaseCanBeChanged() {
        Room room = new Room("Sala1", 2);

        room.setPhase(GamePhase.PLACING_SHIPS);

        assertEquals(GamePhase.PLACING_SHIPS, room.getPhase());
    }

    @Test
    void markPlayerReadyFailsIfUserIsNotPlayer() {
        Room room = new Room("Sala1", 2);
        room.addUser("Bob", RoomRole.SPECTATOR);
        room.setPhase(GamePhase.PLACING_SHIPS);

        boolean ready = room.markPlayerReady("Bob");

        assertFalse(ready);
        assertFalse(room.getReadyPlayers().contains("Bob"));
    }

    @Test
    void markPlayerReadyFailsIfPhaseIsNotPlacingShips() {
        Room room = new Room("Sala1", 2);
        room.addUser("Alice", RoomRole.PLAYER);

        boolean readyWhileWaiting = room.markPlayerReady("Alice");

        assertFalse(readyWhileWaiting);
        assertFalse(room.getReadyPlayers().contains("Alice"));
    }

    @Test
    void markPlayerReadyWorksForPlayerDuringPlacingShips() {
        Room room = new Room("Sala1", 2);
        room.addUser("Alice", RoomRole.PLAYER);
        room.setPhase(GamePhase.PLACING_SHIPS);

        boolean ready = room.markPlayerReady("Alice");

        assertTrue(ready);
        assertTrue(room.getReadyPlayers().contains("Alice"));
    }

    @Test
    void areAllPlayersReadyReturnsFalseIfAtLeastOnePlayerIsMissing() {
        Room room = new Room("Sala1", 2);
        room.addUser("Alice", RoomRole.PLAYER);
        room.addUser("Bob", RoomRole.PLAYER);
        room.setPhase(GamePhase.PLACING_SHIPS);

        room.markPlayerReady("Alice");

        assertFalse(room.areAllPlayersReady());
    }

    @Test
    void areAllPlayersReadyReturnsTrueWhenAllPlayersAreReady() {
        Room room = new Room("Sala1", 2);
        room.addUser("Alice", RoomRole.PLAYER);
        room.addUser("Bob", RoomRole.PLAYER);
        room.setPhase(GamePhase.PLACING_SHIPS);

        room.markPlayerReady("Alice");
        room.markPlayerReady("Bob");

        assertTrue(room.areAllPlayersReady());
    }

    @Test
    void resetReadyPlayersClearsReadySet() {
        Room room = new Room("Sala1", 2);
        room.addUser("Alice", RoomRole.PLAYER);
        room.setPhase(GamePhase.PLACING_SHIPS);

        room.markPlayerReady("Alice");
        assertTrue(room.getReadyPlayers().contains("Alice"));

        room.resetReadyPlayers();

        assertTrue(room.getReadyPlayers().isEmpty());
    }
}