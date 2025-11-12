package xin.bbtt.move;

import org.joml.Vector3d;

/**
 * 移动方向枚举，包含方向向量
 */
public enum Direction {
    Down(0, -1, 0),
    Up(0, 1, 0),
    East(1, 0, 0),
    West(-1, 0, 0),
    South(0, 0, 1),
    North(0, 0, -1),
    NorthEast(1, 0, -1),
    SouthEast(1, 0, 1),
    SouthWest(-1, 0, 1),
    NorthWest(-1, 0, -1);

    private final Vector3d vector;

    Direction(double x, double y, double z) {
        this.vector = new Vector3d(x, y, z);
    }

    public Vector3d getVector() {
        return new Vector3d(vector);
    }

    /**
     * 获取水平方向（用于玩家移动）
     */
    public static Direction[] getHorizontalDirections() {
        return new Direction[]{East, West, South, North};
    }
}