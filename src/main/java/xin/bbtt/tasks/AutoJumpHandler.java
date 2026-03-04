package xin.bbtt.tasks;

import org.joml.Vector3d;
import xin.bbtt.MovementSync;
import xin.bbtt.world.Direction;

/**
 * 自动跳跃处理器
 * 结合原版Minecraft的跳跃机制和MCC的智能路径规划
 */
public class AutoJumpHandler {
    
    // 跳跃检测参数
    private static final double OBSTACLE_HEIGHT_THRESHOLD = 1.0; // 障碍物高度阈值
    private static final double JUMP_COOLDOWN_TIME = 1000; // 跳跃冷却时间(毫秒)
    private static final double SAFE_JUMP_DISTANCE = 2.0; // 安全跳跃距离
    
    // 状态跟踪
    private long lastJumpTime = 0;
    private boolean isAutoJumpEnabled = false;
    private Vector3d targetDirection = new Vector3d(0, 0, 1); // 默认向前
    
    public AutoJumpHandler() {
        // 初始化
    }
    
    /**
     * 启用自动跳跃功能
     */
    public void enableAutoJump() {
        isAutoJumpEnabled = true;
        MovementSync.Instance.getLogger().info("Auto jump enabled");
    }
    
    /**
     * 禁用自动跳跃功能
     */
    public void disableAutoJump() {
        isAutoJumpEnabled = false;
        MovementSync.Instance.getLogger().info("Auto jump disabled");
    }
    
    /**
     * 设置目标方向
     */
    public void setTargetDirection(Vector3d direction) {
        this.targetDirection = new Vector3d(direction).normalize();
    }
    
    /**
     * 主动检查并执行自动跳跃
     * 在物理更新循环中定期调用
     */
    public void checkAndJump() {
        if (!isAutoJumpEnabled || MovementSync.Instance == null) {
            return;
        }
        
        try {
            // 检查是否可以跳跃
            if (!canPerformJump()) {
                return;
            }
            
            // 检查前方是否有障碍物
            if (detectObstacleAhead()) {
                executeSmartJump();
            }
        } catch (Exception e) {
            MovementSync.Instance.getLogger().debug("Auto jump check failed", e);
        }
    }
    
    /**
     * 检查是否可以执行跳跃
     */
    private boolean canPerformJump() {
        // 必须在地面上
        if (!MovementSync.Instance.onGround.get()) {
            return false;
        }
        
        // 检查冷却时间
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastJumpTime < JUMP_COOLDOWN_TIME) {
            return false;
        }
        
        // 检查垂直速度
        Vector3d velocity = MovementSync.Instance.velocity.get();
        return Math.abs(velocity.y) < 0.1;
    }
    
    /**
     * 检测前方是否有障碍物
     */
    private boolean detectObstacleAhead() {
        try {
            Vector3d currentPosition = MovementSync.Instance.position.get();
            Vector3d checkPosition = new Vector3d(currentPosition);
            
            // 沿着移动方向检查几个位置
            for (int i = 1; i <= 3; i++) {
                Vector3d checkPoint = new Vector3d(currentPosition)
                    .add(targetDirection.x * i, 0, targetDirection.z * i);
                
                // 检查脚部位置是否有障碍物
                if (isObstacleAtPosition(checkPoint)) {
                    return true;
                }
                
                // 检查头部位置是否可以通行
                Vector3d headPosition = new Vector3d(checkPoint);
                headPosition.y += 1.8; // 玩家身高
                if (!isPositionClear(headPosition)) {
                    return true;
                }
            }
            
            return false;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 检查指定位置是否有障碍物
     */
    private boolean isObstacleAtPosition(Vector3d position) {
        try {
            // 检查当前位置是否可通行
            if (!isPositionClear(position)) {
                return true;
            }
            
            // 检查上方位置（跳跃高度范围内）
            for (double height = 0.1; height <= OBSTACLE_HEIGHT_THRESHOLD; height += 0.1) {
                Vector3d checkPos = new Vector3d(position);
                checkPos.y += height;
                if (!isPositionClear(checkPos)) {
                    return true;
                }
            }
            
            return false;
        } catch (Exception e) {
            return true; // 出错时保守处理
        }
    }
    
    /**
     * 检查位置是否可通行（无固体方块）
     */
    private boolean isPositionClear(Vector3d position) {
        try {
            return !MovementSync.Instance.world.isOnGround(position);
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 执行智能跳跃
     */
    private void executeSmartJump() {
        try {
            // 计算跳跃力度 - 根据障碍物高度调整
            double jumpPower = calculateOptimalJumpPower();
            
            // 执行跳跃
            Vector3d velocity = new Vector3d(MovementSync.Instance.velocity.get());
            velocity.y = jumpPower;
            
            // 添加向前的动量
            velocity.x += targetDirection.x * 0.1;
            velocity.z += targetDirection.z * 0.1;
            
            MovementSync.Instance.velocity.set(velocity);
            MovementSync.Instance.onGround.set(false);
            
            lastJumpTime = System.currentTimeMillis();
            
            MovementSync.Instance.getLogger().debug(
                "Auto jump executed - Power: {}, Direction: ({}, {})",
                String.format("%.2f", jumpPower),
                String.format("%.2f", targetDirection.x),
                String.format("%.2f", targetDirection.z)
            );
            
        } catch (Exception e) {
            MovementSync.Instance.getLogger().warn("Auto jump execution failed", e);
        }
    }
    
    /**
     * 计算最优跳跃力度
     */
    private double calculateOptimalJumpPower() {
        // 基础跳跃力量
        double basePower = 0.42;
        
        // 根据前方障碍物高度调整
        double obstacleHeight = estimateObstacleHeight();
        
        // 简单的高度补偿公式
        if (obstacleHeight > 0.5) {
            basePower += Math.min(obstacleHeight * 0.3, 0.2); // 最大额外0.2
        }
        
        return Math.min(basePower, 0.6); // 限制最大跳跃力量
    }
    
    /**
     * 估算前方障碍物高度
     */
    private double estimateObstacleHeight() {
        try {
            Vector3d currentPosition = MovementSync.Instance.position.get();
            Vector3d checkPoint = new Vector3d(currentPosition)
                .add(targetDirection.x, 0, targetDirection.z);
            
            // 从地面开始向上检查
            double groundLevel = findGroundLevel(currentPosition);
            double maxHeight = 0;
            
            for (double height = 0.1; height <= 3.0; height += 0.1) {
                Vector3d checkPos = new Vector3d(checkPoint);
                checkPos.y = groundLevel + height;
                
                if (!isPositionClear(checkPos)) {
                    maxHeight = height;
                } else if (maxHeight > 0) {
                    // 找到了障碍物的顶部
                    break;
                }
            }
            
            return maxHeight;
        } catch (Exception e) {
            return 1.0; // 默认障碍物高度
        }
    }
    
    /**
     * 查找地面高度
     */
    private double findGroundLevel(Vector3d position) {
        try {
            Vector3d checkPos = new Vector3d(position);
            checkPos.y = Math.ceil(position.y);
            
            while (checkPos.y > 0) {
                if (MovementSync.Instance.world.isOnGround(checkPos)) {
                    return checkPos.y;
                }
                checkPos.y -= 1.0;
            }
            
            return position.y; // 如果找不到地面，返回当前位置
        } catch (Exception e) {
            return position.y;
        }
    }
    
    /**
     * 获取自动跳跃状态
     */
    public boolean isAutoJumpEnabled() {
        return isAutoJumpEnabled;
    }
    
    /**
     * 获取上次跳跃时间
     */
    public long getLastJumpTime() {
        return lastJumpTime;
    }
}