package xin.bbtt.world;

import lombok.Getter;
import org.joml.Vector3d;
import org.joml.Vector3dc;

@Getter
public enum Direction {
    NORTH(0, 0, -1),
    SOUTH(0, 0, 1),
    WEST(-1, 0, 0),
    EAST(1, 0, 0),
    UP(0, 1, 0),
    DOWN(0, -1, 0);

    private final Vector3dc unitVector;

    Direction(double x, double y, double z) {
        this.unitVector = new Vector3d(x, y, z);
    }

    public Vector3d getVector(double distance) {
        return new Vector3d(unitVector).mul(distance);
    }

}