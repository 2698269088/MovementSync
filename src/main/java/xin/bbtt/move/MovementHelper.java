package xin.bbtt.move;

import org.joml.Vector3d;
import xin.bbtt.MovementSync;
import xin.bbtt.world.Block;
import xin.bbtt.world.Material;
import xin.bbtt.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * MCC移动系统完整移植 - 包含物理模拟和碰撞检测
 * 所有方法均为静态，便于调用
 */
public class MovementHelper {
    private static final double EPSILON = 0.001; // 增加容差值
    private static final double GRAVITY = -0.08;
    private static final double DRAG = 0.9800000190734863;
    private static final double TERMINAL_VELOCITY = -3.92;

    /**
     * 检测是否在地面上
     * 支持藤蔓、梯子等特殊方块，带边缘检测
     */
    public static boolean isOnGround(World world, Vector3d position) {
        // 如果区块未加载，则认为在地面上以避免意外下落
        if (world == null) return true;

        // 检查正下方方块
        Vector3d down = move(position, Direction.Down);
        Block downBlock = getBlock(world, down);
        Material material = downBlock.getType();

        boolean result = material.isSolid() ||
                material == Material.TwistingVines ||
                material == Material.TwistingVinesPlant ||
                material == Material.WeepingVines ||
                material == Material.WeepingVinesPlant ||
                material == Material.Vine ||
                material == Material.Ladder;

        // 边缘检测 - 检查对角方块
        double x = position.x;
        double y = position.y;
        double z = position.z;
        
        // 检查周围方块（更宽松的检测）
        boolean northCheck = 1 + Math.floor(z) - z > 0.7;
        boolean eastCheck = x - Math.floor(x) > 0.7;
        boolean southCheck = z - Math.floor(z) > 0.7;
        boolean westCheck = 1 + Math.floor(x) - x > 0.7;

        if (!result && northCheck) result |= isSolidOrVine(world, move(down, Direction.North));
        if (!result && northCheck && eastCheck) result |= isSolidOrVine(world, move(down, Direction.NorthEast));
        if (!result && eastCheck) result |= isSolidOrVine(world, move(down, Direction.East));
        if (!result && eastCheck && southCheck) result |= isSolidOrVine(world, move(down, Direction.SouthEast));
        if (!result && southCheck) result |= isSolidOrVine(world, move(down, Direction.South));
        if (!result && southCheck && westCheck) result |= isSolidOrVine(world, move(down, Direction.SouthWest));
        if (!result && westCheck) result |= isSolidOrVine(world, move(down, Direction.West));
        if (!result && westCheck && northCheck) result |= isSolidOrVine(world, move(down, Direction.NorthWest));

        // 检查是否在方块顶部
        return result && (y <= Math.floor(y) + 0.0005);
    }

    private static boolean isSolidOrVine(World world, Vector3d location) {
        Material type = getBlock(world, location).getType();
        return type.isSolid() || type.canBeClimbedOn();
    }

    /**
     * 检查是否可以朝指定方向移动
     */
    public static boolean canMove(World world, Vector3d location, Direction direction) {
        switch (direction) {
            // 垂直移动
            case Down:
                return isClimbing(world, move(location, Direction.Down)) || !isOnGround(world, location);
            case Up:
                boolean nextTwoBlocks = !getBlock(world, move(move(location, Direction.Up), Direction.Up)).getType().isSolid();
                
                // 检查当前方块是否可以攀爬
                if (isClimbing(world, location))
                    // 检查下一个方块是否可以攀爬
                    return isClimbing(world, move(location, Direction.Up)) || nextTwoBlocks;
                
                return (isOnGround(world, location) || isSwimming(world, location)) && nextTwoBlocks;

            // 水平移动
            case East:
            case West:
            case South:
            case North:
                return playerFitsHere(world, move(location, direction));

            // 对角移动
            case NorthEast:
                return playerFitsHere(world, move(location, Direction.North)) &&
                       playerFitsHere(world, move(location, Direction.East)) &&
                       playerFitsHere(world, move(location, direction));
            case SouthEast:
                return playerFitsHere(world, move(location, Direction.South)) &&
                       playerFitsHere(world, move(location, Direction.East)) &&
                       playerFitsHere(world, move(location, direction));
            case SouthWest:
                return playerFitsHere(world, move(location, Direction.South)) &&
                       playerFitsHere(world, move(location, Direction.West)) &&
                       playerFitsHere(world, move(location, direction));
            case NorthWest:
                return playerFitsHere(world, move(location, Direction.North)) &&
                       playerFitsHere(world, move(location, Direction.West)) &&
                       playerFitsHere(world, move(location, direction));

            default:
                return false;
        }
    }

    /**
     * 检查玩家是否可以在指定位置站立（不会卡住）
     */
    public static boolean playerFitsHere(World world, Vector3d location) {
        boolean canClimb = isClimbing(world, location) && isClimbing(world, move(location, Direction.Up));
        boolean isNotSolid = !getBlock(world, location).getType().isSolid() &&
                             !getBlock(world, move(location, Direction.Up)).getType().isSolid();
        
        // 处理半砖
        if (!isNotSolid && getBlock(world, move(location, Direction.Up)).isTopSlab()) {
            isNotSolid = true;
        }
        
        return canClimb || isNotSolid;
    }

    /**
     * 检查前方是否有障碍物阻挡移动
     */
    public static boolean willCollide(World world, Vector3d position, Direction direction) {
        Vector3d target = move(position, direction);
        Vector3d eyePosition = new Vector3d(position.x, position.y + 1.62, position.z); // 眼睛高度
        Vector3d targetEye = new Vector3d(target.x, target.y + 1.62, target.z);
        
        // 检查眼睛高度是否有障碍物
        if (!playerFitsHere(world, targetEye)) {
            return true;
        }
        
        // 检查脚下是否有障碍物
        if (!playerFitsHere(world, target)) {
            return true;
        }
        
        return false;
    }

    // ==================== 工具方法 ====================

    public static Vector3d move(Vector3d location, Direction direction) {
        return location.add(direction.getVector(), new Vector3d());
    }

    public static Vector3d move(Vector3d location, Direction direction, int length) {
        return location.add(direction.getVector().mul(length, new Vector3d()), new Vector3d());
    }

    private static Block getBlock(World world, Vector3d location) {
        return world.getBlockAt(location);
    }

    public static Vector3d toCenter(Vector3d location) {
        return new Vector3d(Math.floor(location.x) + 0.5, location.y, Math.floor(location.z) + 0.5);
    }

    private static boolean isSwimming(World world, Vector3d location) {
        return getBlock(world, location).getType().isLiquid();
    }

    private static boolean isClimbing(World world, Vector3d location) {
        return getBlock(world, location).getType().canBeClimbedOn();
    }

    private static double getMinWorldHeight() {
        return -64.0; // 1.18+ 默认最小高度
    }

    private static Direction[] getHorizontalDirections() {
        return new Direction[]{Direction.East, Direction.West, Direction.South, Direction.North};
    }
}