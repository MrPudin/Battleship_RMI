package battleship.model;

import java.io.Serializable;

public enum ResultantShot implements Serializable {
    MISS,
    HIT,
    SUNK,
    REPEATED
}