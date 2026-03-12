package battleship.model;

public enum ShipType {
    BOAT(2),
    FRIGATE(3),
    CRUISER(4),
    AIRCRAFTCARRIER(5);

    private final int size;

    ShipType(int size) {
        this.size = size;
    }

    public int getSize() {
        return size;
    }
}