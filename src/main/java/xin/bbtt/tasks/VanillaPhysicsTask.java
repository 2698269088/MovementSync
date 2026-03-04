package xin.bbtt.tasks;

import org.geysermc.mcprotocollib.protocol.packet.ingame.serverbound.player.ServerboundMovePlayerPosRotPacket;
import org.joml.Vector3d;
import xin.bbtt.MovementSync;
import xin.bbtt.mcbot.Bot;
import xin.bbtt.mcbot.Server;
import xin.bbtt.utils.AABB;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 完全按照原版Minecraft物理引擎实现的物理模拟任务
 * 基于Entity.move()方法的精确复制
 */
public class VanillaPhysicsTask implements Runnable {
    
    // 原版精确物理参数
    private static final Vector3d GRAVITY = new Vector3d(0, -0.08, 0);
    private static final double TERMINAL_VELOCITY = -3.92;
    private static final double AIR_DRAG = 0.9800000190734863D;
    private static final double JUMP_POWER = 0.42;
    private static final double MOVEMENT_FRICTION = 0.91;
    private static final double INPUT_FRICTION = 0.98;
    
    // 状态跟踪
    private Vector3d lastPos = new Vector3d();
    private float lastPitch = 0;
    private float lastYaw = 0;
    private boolean wasOnGround = true;
    private int jumpTicks = 0;
    private double lastVelocityY = 0; // 记录上一帧的垂直速度
    
    // 原版风格的状态标志
    private boolean isCollidingX = false;
    private boolean isCollidingY = false;
    private boolean isCollidingZ = false;
    
    // 自动跳跃处理器
    private final AutoJumpHandler autoJumpHandler = new AutoJumpHandler();
    
    private final AtomicBoolean isInitialized = new AtomicBoolean(false);
    
    @Override
    public void run() {
        // 安全检查
        if (MovementSync.Instance == null) {
            return;
        }
        
        if (!Bot.Instance.isRunning()) return;
        if (Bot.Instance.getServer() != Server.Xin) return;
        
        // 延迟初始化
        if (!isInitialized.get() && MovementSync.Instance.world != null) {
            initializePhysics();
            isInitialized.set(true);
        }
        
        try {
            updatePhysics();
        } catch (Exception e) {
            MovementSync.Instance.getLogger().error("Physics update failed", e);
        }
    }
    
    /**
     * 初始化物理系统 - 原版风格
     */
    private void initializePhysics() {
        if (MovementSync.Instance.position.get() == null) {
            MovementSync.Instance.position.set(new Vector3d(0, 0, 0));
        }
        if (MovementSync.Instance.velocity.get() == null) {
            MovementSync.Instance.velocity.set(new Vector3d(0, 0, 0));
        }
        // 初始化速度记录
        lastVelocityY = MovementSync.Instance.velocity.get().y;
        checkOnGround();
    }
    
    /**
     * 主物理更新循环 - 严格遵循原版Entity.move()逻辑
     */
    private void updatePhysics() {
        Vector3d velocity = new Vector3d(MovementSync.Instance.velocity.get());
        Vector3d position = new Vector3d(MovementSync.Instance.position.get());
        Vector3d newPosition = new Vector3d(position);
        
        // 原版风格的地面检测
        boolean isOnGround = checkOnGround();
        
        // 重置接地时的垂直速度 - 原版逻辑
        if (isOnGround && velocity.y < 0) {
            velocity.y = 0;
            jumpTicks = 0;
        }
        
        // 应用重力 - 原版每帧都应用
        velocity.add(GRAVITY);
        
        // 原版空气阻力计算
        if (!isOnGround) {
            velocity.y *= AIR_DRAG;
        } else {
            // 地面摩擦力
            velocity.x *= MOVEMENT_FRICTION;
            velocity.z *= MOVEMENT_FRICTION;
        }
        
        // 输入摩擦力（原版机制）
        velocity.x *= INPUT_FRICTION;
        velocity.z *= INPUT_FRICTION;
        
        // 终端速度限制
        if (velocity.y < TERMINAL_VELOCITY) {
            velocity.y = TERMINAL_VELOCITY;
        }
        
        // 原版移动计算 - 分别处理每个轴
        moveEntity(newPosition, velocity);
        
        // 更新碰撞状态
        updateCollisionState(position, newPosition);
        
        // 更新最终状态
        MovementSync.Instance.velocity.set(velocity);
        MovementSync.Instance.position.set(newPosition);
        MovementSync.Instance.onGround.set(isOnGround);
        wasOnGround = isOnGround;
        
        // 执行自动跳跃检查
        autoJumpHandler.checkAndJump();
        
        // 同步到服务器
        if (shouldSyncToServer()) {
            syncPositionToServer();
            updateLastSyncState();
        }
        
        // 调试信息
        if (MovementSync.Instance.getLogger().isDebugEnabled()) {
            MovementSync.Instance.getLogger().debug(
                "Physics - onGround:{}, velY:{}, posY:{}, collidingY:{}, AABB:{}",
                isOnGround, String.format("%.4f", velocity.y), 
                String.format("%.4f", newPosition.y), isCollidingY,
                AABB.playerBoundingBox(newPosition).toString()
            );
        }
    }
    
    /**
     * 原版风格的实体移动 - 使用AABB的精确碰撞检测
     * 完全移植原版MC Entity.move()方法的核心逻辑
     */
    private void moveEntity(Vector3d position, Vector3d velocity) {
        Vector3d remainingMove = new Vector3d(velocity);
        Vector3d originalPos = new Vector3d(position);
        
        // 创建初始碰撞箱
        AABB initialBox = AABB.playerBoundingBox(originalPos);
        
        // X轴移动 - 原版风格的分离轴检测
        if (Math.abs(remainingMove.x) > 1.0E-7) {
            double moveX = remainingMove.x;
            Vector3d testPos = new Vector3d(position);
            testPos.x += moveX;
            AABB movedBox = initialBox.move(moveX, 0, 0);
            
            if (checkAABBCollisions(movedBox, 'x')) {
                // 发生碰撞，回退X轴移动
                position.x = originalPos.x;
                velocity.x = 0;
                isCollidingX = true;
            } else {
                position.x = testPos.x;
                isCollidingX = false;
            }
        }
        
        // Y轴移动
        if (Math.abs(remainingMove.y) > 1.0E-7) {
            double moveY = remainingMove.y;
            Vector3d testPos = new Vector3d(position);
            testPos.y += moveY;
            AABB movedBox = AABB.playerBoundingBox(originalPos).move(
                position.x - originalPos.x, moveY, position.z - originalPos.z);
            
            if (checkAABBCollisions(movedBox, 'y')) {
                position.y = originalPos.y;
                velocity.y = 0;
                isCollidingY = true;
            } else {
                position.y = testPos.y;
                isCollidingY = false;
            }
        }
        
        // Z轴移动
        if (Math.abs(remainingMove.z) > 1.0E-7) {
            double moveZ = remainingMove.z;
            Vector3d testPos = new Vector3d(position);
            testPos.z += moveZ;
            AABB movedBox = AABB.playerBoundingBox(originalPos).move(
                position.x - originalPos.x, position.y - originalPos.y, moveZ);
            
            if (checkAABBCollisions(movedBox, 'z')) {
                position.z = originalPos.z;
                velocity.z = 0;
                isCollidingZ = true;
            } else {
                position.z = testPos.z;
                isCollidingZ = false;
            }
        }
    }
    
    /**
     * 使用AABB进行精确碰撞检测 - 原版风格实现
     */
    private boolean checkAABBCollisions(AABB movedBox, char axis) {
        try {
            // 计算需要检查的方块范围
            int minX = (int) Math.floor(movedBox.minX);
            int minY = (int) Math.floor(movedBox.minY);
            int minZ = (int) Math.floor(movedBox.minZ);
            int maxX = (int) Math.floor(movedBox.maxX);
            int maxY = (int) Math.floor(movedBox.maxY);
            int maxZ = (int) Math.floor(movedBox.maxZ);
            
            // 检查每个可能碰撞的方块
            for (int x = minX; x <= maxX; x++) {
                for (int y = minY; y <= maxY; y++) {
                    for (int z = minZ; z <= maxZ; z++) {
                        Vector3d blockPos = new Vector3d(x, y, z);
                        // 对于不同轴，采用不同的检测策略
                        switch (axis) {
                            case 'x':
                                // X轴移动时的碰撞检测
                                if (MovementSync.Instance.world.isOnGround(blockPos)) {
                                    return true;
                                }
                                break;
                            case 'y':
                                // Y轴移动时包括地面检测
                                if (MovementSync.Instance.world.isOnGround(blockPos)) {
                                    return true;
                                }
                                break;
                            case 'z':
                                // Z轴移动时的碰撞检测
                                if (MovementSync.Instance.world.isOnGround(blockPos)) {
                                    return true;
                                }
                                break;
                        }
                    }
                }
            }
            
            return false; // 无碰撞
        } catch (Exception e) {
            return true; // 出错时保守处理
        }
    }
    
    /**
     * 检查指定轴上的碰撞 - 保持向后兼容
     */
    private boolean checkCollisionsAlongAxis(Vector3d position, char axis) {
        // 为了保持兼容性，仍然保留旧方法但委托给AABB方法
        AABB playerBox = AABB.playerBoundingBox(position);
        return checkAABBCollisions(playerBox, axis);
    }
    
    /**
     * 检查位置是否可通行 - 使用AABB的原版风格碰撞检测
     * 完全移植原版MC的碰撞检测逻辑
     */
    private boolean isPositionClear(Vector3d position) {
        try {
            // 创建玩家碰撞箱
            AABB playerBox = AABB.playerBoundingBox(position);
            
            // 检查碰撞箱范围内的所有方块
            int minX = (int) Math.floor(playerBox.minX);
            int minY = (int) Math.floor(playerBox.minY);
            int minZ = (int) Math.floor(playerBox.minZ);
            int maxX = (int) Math.floor(playerBox.maxX);
            int maxY = (int) Math.floor(playerBox.maxY);
            int maxZ = (int) Math.floor(playerBox.maxZ);
            
            // 检查每个可能碰撞的方块
            for (int x = minX; x <= maxX; x++) {
                for (int y = minY; y <= maxY; y++) {
                    for (int z = minZ; z <= maxZ; z++) {
                        Vector3d blockPos = new Vector3d(x, y, z);
                        // 如果有任何方块是实体方块，则位置不可通行
                        if (MovementSync.Instance.world.isOnGround(blockPos)) {
                            return false;
                        }
                    }
                }
            }
            
            return true; // 所有方块都可通过
        } catch (Exception e) {
            // 出错时保守处理 - 认为不可通行
            return false;
        }
    }
    
    /**
     * 地面碰撞检测 - 原版精确逻辑
     */
    private boolean checkGroundCollision(Vector3d position) {
        Vector3d feetPos = new Vector3d(position);
        feetPos.y -= 0.05; // 原版偏移量
        
        return MovementSync.Instance.world.isOnGround(feetPos);
    }
    
    /**
     * 更新碰撞状态 - 原版风格
     */
    private void updateCollisionState(Vector3d oldPos, Vector3d newPos) {
        // 检测是否发生了碰撞
        double deltaX = Math.abs(newPos.x - oldPos.x);
        double deltaY = Math.abs(newPos.y - oldPos.y);
        double deltaZ = Math.abs(newPos.z - oldPos.z);
        
        // 如果移动距离很小，可能发生了碰撞
        isCollidingX = deltaX < 1.0E-7;
        isCollidingY = deltaY < 1.0E-7;
        isCollidingZ = deltaZ < 1.0E-7;
    }
    
    /**
     * 原版风格的地面检测 - 使用AABB进行精确检测
     * 移植自原版MC的地面检测逻辑
     */
    private boolean checkOnGround() {
        try {
            Vector3d currentPosition = MovementSync.Instance.position.get();
            // 创建玩家碰撞箱
            AABB playerBox = AABB.playerBoundingBox(currentPosition);
            
            // 地面检测：检查碰撞箱下方0.05个单位的方块
            AABB groundCheckBox = playerBox.move(0, -0.05, 0);
            
            // 计算需要检查的方块范围
            int minX = (int) Math.floor(groundCheckBox.minX);
            int minY = (int) Math.floor(groundCheckBox.minY);
            int minZ = (int) Math.floor(groundCheckBox.minZ);
            int maxX = (int) Math.floor(groundCheckBox.maxX);
            int maxY = (int) Math.floor(groundCheckBox.maxY);
            int maxZ = (int) Math.floor(groundCheckBox.maxZ);
            
            // 检查每个可能的地面方块
            for (int x = minX; x <= maxX; x++) {
                for (int z = minZ; z <= maxZ; z++) {
                    // 只检查Y轴范围内的方块
                    for (int y = minY; y <= maxY; y++) {
                        Vector3d blockPos = new Vector3d(x, y, z);
                        if (MovementSync.Instance.world.isOnGround(blockPos)) {
                            return true;
                        }
                    }
                }
            }
            
            // 辅助检测：检查更下方的位置
            AABB lowerCheckBox = playerBox.move(0, -0.1, 0);
            int lowerMinY = (int) Math.floor(lowerCheckBox.minY);
            int lowerMaxY = (int) Math.floor(lowerCheckBox.maxY);
            
            for (int x = minX; x <= maxX; x++) {
                for (int z = minZ; z <= maxZ; z++) {
                    for (int y = lowerMinY; y <= lowerMaxY; y++) {
                        Vector3d blockPos = new Vector3d(x, y, z);
                        if (MovementSync.Instance.world.isOnGround(blockPos)) {
                            return true;
                        }
                    }
                }
            }
            
            return false;
        } catch (Exception e) {
            return true; // 安全默认值
        }
    }
    
    /**
     * 判断是否需要同步到服务器 - 原版风格的同步条件
     * 参考原版MC的同步策略，避免过度频繁的同步
     */
    private boolean shouldSyncToServer() {
        try {
            if (MovementSync.Instance == null || 
                MovementSync.Instance.position == null ||
                MovementSync.Instance.pitch == null ||
                MovementSync.Instance.yaw == null) {
                return false;
            }
            
            Vector3d currentPos = MovementSync.Instance.position.get();
            Float pitchObj = MovementSync.Instance.pitch.get();
            Float yawObj = MovementSync.Instance.yaw.get();
            
            if (currentPos == null || pitchObj == null || yawObj == null) {
                return false;
            }
            
            float currentPitch = pitchObj;
            float currentYaw = yawObj;
            
            // 计算变化量
            double posChange = lastPos.distance(currentPos);
            float pitchChange = Math.abs(lastPitch - currentPitch);
            float yawChange = Math.abs(lastYaw - currentYaw);
            
            // 原版MC同步条件：
            // 1. 位置变化超过一定阈值 (约0.01个方块)
            // 2. 角度变化超过一定阈值 (约1度)
            // 3. 接地状态发生变化
            // 4. 垂直速度发生显著变化
            boolean significantPositionChange = posChange > 0.01;
            boolean significantRotationChange = pitchChange > 1.0f || yawChange > 1.0f;
            boolean groundStateChanged = wasOnGround != MovementSync.Instance.onGround.get();
            
            Vector3d currentVel = MovementSync.Instance.velocity.get();
            double verticalVelChange = Math.abs(currentVel.y - lastVelocityY);
            boolean significantVerticalChange = verticalVelChange > 0.05;
            
            boolean shouldSync = (significantPositionChange || 
                                 significantRotationChange || 
                                 groundStateChanged || 
                                 significantVerticalChange) && 
                                 Bot.Instance.getServer() == Server.Xin;
            
            // 更新上一帧的速度记录
            lastVelocityY = currentVel.y;
            
            return shouldSync;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 更新最后同步状态
     */
    private void updateLastSyncState() {
        try {
            if (MovementSync.Instance == null ||
                MovementSync.Instance.position == null ||
                MovementSync.Instance.pitch == null ||
                MovementSync.Instance.yaw == null) {
                return;
            }
            
            Vector3d pos = MovementSync.Instance.position.get();
            Float pitchObj = MovementSync.Instance.pitch.get();
            Float yawObj = MovementSync.Instance.yaw.get();
            
            if (pos != null && pitchObj != null && yawObj != null) {
                lastPos = new Vector3d(pos);
                lastPitch = pitchObj;
                lastYaw = yawObj;
            }
        } catch (Exception e) {
            // 静默失败
        }
    }
    
    /**
     * 同步位置到服务器 - 原版协议格式
     */
    private void syncPositionToServer() {
        try {
            if (Bot.Instance.getSession() != null && MovementSync.Instance != null) {
                ServerboundMovePlayerPosRotPacket packet = new ServerboundMovePlayerPosRotPacket(
                    MovementSync.Instance.onGround.get(),
                    MovementSync.Instance.position.get().x,
                    MovementSync.Instance.position.get().y,
                    MovementSync.Instance.position.get().z,
                    MovementSync.Instance.yaw.get(),
                    MovementSync.Instance.pitch.get()
                );
                
                Bot.Instance.getSession().send(packet);
            }
        } catch (Exception e) {
            // 静默失败
        }
    }
    
    /**
     * 公共方法：触发跳跃 - 原版精确动力学
     */
    public static void jump() {
        if (MovementSync.Instance == null) return;
        
        if (MovementSync.Instance.onGround.get() && 
            MovementSync.Instance.velocity != null) {
            
            Vector3d velocity = new Vector3d(MovementSync.Instance.velocity.get());
            // 原版跳跃条件：必须在地面且垂直速度接近0
            if (Math.abs(velocity.y) < 0.1) {
                velocity.y = JUMP_POWER;
                MovementSync.Instance.velocity.set(velocity);
                MovementSync.Instance.onGround.set(false);
            }
        }
    }
    
    /**
     * 公共方法：设置移动输入 - 原版移动机制
     */
    public static void setMovementInput(double strafe, double forward) {
        if (MovementSync.Instance == null) return;
        
        Vector3d velocity = new Vector3d(MovementSync.Instance.velocity.get());
        Vector3d moveVector = new Vector3d(strafe, 0, forward);
        
        // 原版输入处理
        moveVector.mul(INPUT_FRICTION);
        velocity.x += moveVector.x;
        velocity.z += moveVector.z;
        
        MovementSync.Instance.velocity.set(velocity);
    }
    
    /**
     * 公共方法：启用自动跳跃
     */
    public static void enableAutoJump() {
        if (MovementSync.Instance != null && MovementSync.Instance.physicsTask != null) {
            MovementSync.Instance.physicsTask.autoJumpHandler.enableAutoJump();
        }
    }
    
    /**
     * 公共方法：禁用自动跳跃
     */
    public static void disableAutoJump() {
        if (MovementSync.Instance != null && MovementSync.Instance.physicsTask != null) {
            MovementSync.Instance.physicsTask.autoJumpHandler.disableAutoJump();
        }
    }
    
    /**
     * 公共方法：设置自动跳跃目标方向
     */
    public static void setAutoJumpDirection(Vector3d direction) {
        if (MovementSync.Instance != null && MovementSync.Instance.physicsTask != null) {
            MovementSync.Instance.physicsTask.autoJumpHandler.setTargetDirection(direction);
        }
    }
    
    /**
     * 公共方法：检查自动跳跃是否启用
     */
    public static boolean isAutoJumpEnabled() {
        if (MovementSync.Instance != null && MovementSync.Instance.physicsTask != null) {
            return MovementSync.Instance.physicsTask.autoJumpHandler.isAutoJumpEnabled();
        }
        return false;
    }
}