package xin.bbtt.move;

import org.joml.Vector3d;
import xin.bbtt.world.World;

public class Physics {
    
    // 重力加速度 (m/s²)
    private static final double GRAVITY = 0.08D;
    
    // 空气阻力系数
    private static final double AIR_RESISTANCE = 0.9800000190734863D;
    
    // 玩家站立时的碰撞箱高度
    private static final double PLAYER_HEIGHT = 1.8D;
    
    // 玩家宽度（碰撞箱）
    private static final double PLAYER_WIDTH = 0.6D;
    
    // 方块是否为固体的简单判断
    private static boolean isSolidBlock(int blockY) {
        // 简化处理：Y<=0认为是固体方块（地面）
        // 在实际实现中，应该检查World中的具体方块信息
        return blockY <= 0;
    }
    
    /**
     * 处理重力影响
     * @param world 当前世界
     * @param position 当前位置
     * @param motionY Y轴动量
     * @return 更新后的位置和动量
     */
    public static PhysicsResult handleGravity(World world, Vector3d position, double motionY) {
        // 先检查是否已经在地面上
        if (isOnGround(world, position)) {
            // 在地面上时重置动量
            motionY = 0;
        } else {
            // 应用重力
            motionY -= GRAVITY;
            motionY *= AIR_RESISTANCE;
            position.y += motionY;
            
            // 检查下落过程中是否碰撞到地面
            if (isOnGround(world, new Vector3d(position.x, position.y, position.z))) {
                // 调整位置到地面上，确保玩家站在方块顶部
                position.y = Math.floor(position.y) + 1.0; // 站在方块顶部
                motionY = 0;
            }
        }
        
        // 添加额外的地面检查，确保不会陷入地下
        if (position.y < 1.0 && isSolidBlock((int) Math.floor(position.y - 0.01))) {
            position.y = 1.0;
            motionY = 0;
        }
        
        return new PhysicsResult(position, motionY);
    }
    
    /**
     * 检查玩家是否在地面上
     * @param world 当前世界
     * @param position 当前位置
     * @return 是否在地面上
     */
    public static boolean isOnGround(World world, Vector3d position) {
        // 检查脚下的方块，使用更精确的检测方法，参考MCC的实现
        double minX = position.x - PLAYER_WIDTH / 2;
        double maxX = position.x + PLAYER_WIDTH / 2;
        double minZ = position.z - PLAYER_WIDTH / 2;
        double maxZ = position.z + PLAYER_WIDTH / 2;
        // 使用与MCC类似的检测方法，检查脚部位置
        double y = position.y - 0.01; // 稍微向下检查一点，确保接触地面
        
        // 检查当前位置下面的方块是否为固体（检查玩家脚部占据的所有方块）
        for (double x = minX; x <= maxX; x += 0.3) {
            for (double z = minZ; z <= maxZ; z += 0.3) {
                int blockX = (int) Math.floor(x);
                int blockY = (int) Math.floor(y);
                int blockZ = (int) Math.floor(z);
                
                if (isSolidBlock(blockY)) {
                    // 确保位置正确，站在方块顶部
                    return position.y >= blockY + 1 && position.y <= blockY + 1.05;
                }
            }
        }
        
        return false;
    }
    
    /**
     * 检查指定位置是否为空气（可以穿过）
     * @param world 当前世界
     * @param position 检查的位置
     * @return 是否为空气
     */
    public static boolean isAir(World world, Vector3d position) {
        // 检查玩家身体占据的区域是否为空气
        double minX = position.x - PLAYER_WIDTH / 2;
        double maxX = position.x + PLAYER_WIDTH / 2;
        double minY = position.y;
        double maxY = position.y + PLAYER_HEIGHT;
        double minZ = position.z - PLAYER_WIDTH / 2;
        double maxZ = position.z + PLAYER_WIDTH / 2;
        
        // 检查玩家身体占据的所有方块
        for (double x = minX; x <= maxX; x += 0.3) {
            for (double y = minY; y <= maxY; y += 0.3) {
                for (double z = minZ; z <= maxZ; z += 0.3) {
                    int blockX = (int) Math.floor(x);
                    int blockY = (int) Math.floor(y);
                    int blockZ = (int) Math.floor(z);
                    
                    // 如果有任何一个方块是固体，则该位置不可穿过
                    if (isSolidBlock(blockY)) {
                        return false;
                    }
                }
            }
        }
        
        return true;
    }
    
    /**
     * 检查玩家是否可以移动到指定位置
     * @param world 当前世界
     * @param currentPosition 当前位置
     * @param newPosition 目标位置
     * @return 是否可以移动
     */
    public static boolean canMoveTo(World world, Vector3d currentPosition, Vector3d newPosition) {
        // 检查目标位置是否可以穿过（检查玩家身体占据的区域）
        return isAir(world, newPosition);
    }
    
    /**
     * 将移动分解为多个小步骤
     * @param start 起始位置
     * @param goal 目标位置
     * @param motionY Y轴动量
     * @param falling 是否正在下落
     * @param stepsByBlock 每个方块的步数
     * @return 位置队列
     */
    public static Vector3d[] moveInSteps(Vector3d start, Vector3d goal, double motionY, boolean falling, int stepsByBlock) {
        if (stepsByBlock <= 0) {
            stepsByBlock = 1;
        }
        
        if (falling) {
            // 使用类似Minecraft的下落算法
            double y = start.y;
            motionY -= 0.08D;
            motionY *= 0.9800000190734863D;
            y += motionY;
            
            // 限制下落速度，避免过快下降
            double maxFallDistance = 0.1; // 每步最大下落距离
            if (y < start.y - maxFallDistance) {
                y = start.y - maxFallDistance;
            }
            
            if (y < goal.y) {
                return new Vector3d[] { goal };
            }
            
            return new Vector3d[] { new Vector3d(start.x, y, start.z) };
        } else {
            // 常规移动算法
            motionY = 0; // 重置动量
            double totalDistance = start.distance(goal);
            int totalSteps = (int) Math.ceil(totalDistance * stepsByBlock);
            
            if (totalSteps <= 0) {
                return new Vector3d[] { goal };
            }
            
            Vector3d[] steps = new Vector3d[totalSteps];
            Vector3d step = goal.sub(start, new Vector3d()).div(totalSteps);
            
            for (int i = 1; i <= totalSteps; i++) {
                steps[i-1] = start.add(step.mul(i, new Vector3d()), new Vector3d());
            }
            
            return steps;
        }
    }
    
    /**
     * 物理计算结果
     */
    public static class PhysicsResult {
        public final Vector3d position;
        public final double motionY;
        
        public PhysicsResult(Vector3d position, double motionY) {
            this.position = position;
            this.motionY = motionY;
        }
    }
}