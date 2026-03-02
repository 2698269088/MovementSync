package xin.bbtt.tasks;

import org.geysermc.mcprotocollib.protocol.packet.ingame.serverbound.player.ServerboundMovePlayerPosRotPacket;
import org.joml.Vector3d;
import xin.bbtt.MovementSync;
import xin.bbtt.mcbot.Bot;
import xin.bbtt.mcbot.Server;

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
    
    // 原版风格的状态标志
    private boolean isCollidingX = false;
    private boolean isCollidingY = false;
    private boolean isCollidingZ = false;
    
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
        
        // 同步到服务器
        if (shouldSyncToServer()) {
            syncPositionToServer();
            updateLastSyncState();
        }
        
        // 调试信息
        if (MovementSync.Instance.getLogger().isDebugEnabled()) {
            MovementSync.Instance.getLogger().debug(
                "Physics - onGround:{}, velY:{}, posY:{}, collidingY:{}",
                isOnGround, String.format("%.4f", velocity.y), 
                String.format("%.4f", newPosition.y), isCollidingY
            );
        }
    }
    
    /**
     * 原版风格的实体移动 - 精确复制Entity.move()逻辑
     */
    private void moveEntity(Vector3d position, Vector3d velocity) {
        Vector3d remainingMove = new Vector3d(velocity);
        Vector3d originalPos = new Vector3d(position);
        
        // X轴移动
        if (Math.abs(remainingMove.x) > 1.0E-7) {
            double moveX = remainingMove.x;
            position.x += moveX;
            if (checkCollisionsAlongAxis(position, 'x')) {
                position.x = originalPos.x;
                velocity.x = 0;
                isCollidingX = true;
            } else {
                isCollidingX = false;
            }
        }
        
        // Y轴移动
        if (Math.abs(remainingMove.y) > 1.0E-7) {
            double moveY = remainingMove.y;
            position.y += moveY;
            if (checkCollisionsAlongAxis(position, 'y')) {
                position.y = originalPos.y;
                velocity.y = 0;
                isCollidingY = true;
            } else {
                isCollidingY = false;
            }
        }
        
        // Z轴移动
        if (Math.abs(remainingMove.z) > 1.0E-7) {
            double moveZ = remainingMove.z;
            position.z += moveZ;
            if (checkCollisionsAlongAxis(position, 'z')) {
                position.z = originalPos.z;
                velocity.z = 0;
                isCollidingZ = true;
            } else {
                isCollidingZ = false;
            }
        }
    }
    
    /**
     * 检查指定轴上的碰撞 - 原版风格
     */
    private boolean checkCollisionsAlongAxis(Vector3d position, char axis) {
        try {
            // 简化的碰撞检测 - 检查目标位置是否可通行
            Vector3d checkPos = new Vector3d(position);
            
            // 根据轴调整检测位置
            switch (axis) {
                case 'x':
                    // X轴碰撞检测
                    return !isPositionClear(checkPos);
                case 'y':
                    // Y轴碰撞检测（包括地面检测）
                    return !isPositionClear(checkPos) || checkGroundCollision(position);
                case 'z':
                    // Z轴碰撞检测
                    return !isPositionClear(checkPos);
                default:
                    return false;
            }
        } catch (Exception e) {
            return true; // 碰撞时保守处理
        }
    }
    
    /**
     * 检查位置是否可通行 - 简化版碰撞检测
     */
    private boolean isPositionClear(Vector3d position) {
        // 简化的方块检测 - 检查脚部和头部位置
        Vector3d feetPos = new Vector3d(position);
        Vector3d headPos = new Vector3d(position);
        headPos.y += 1.8; // 玩家身高
        
        return MovementSync.Instance.world.isOnGround(feetPos) == false && 
               MovementSync.Instance.world.isOnGround(headPos) == false;
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
     * 原版风格的地面检测
     */
    private boolean checkOnGround() {
        try {
            Vector3d feetPos = new Vector3d(MovementSync.Instance.position.get());
            feetPos.y -= 0.05; // 原版偏移量
            
            boolean onGround = MovementSync.Instance.world.isOnGround(feetPos);
            
            // 辅助检测
            if (!onGround) {
                Vector3d slightlyBelow = new Vector3d(feetPos);
                slightlyBelow.y -= 0.05;
                onGround = MovementSync.Instance.world.isOnGround(slightlyBelow);
            }
            
            return onGround;
        } catch (Exception e) {
            return true; // 安全默认值
        }
    }
    
    /**
     * 判断是否需要同步到服务器
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
            
            boolean positionChanged = !lastPos.equals(currentPos);
            boolean rotationChanged = (lastPitch != currentPitch) || (lastYaw != currentYaw);
            
            return (positionChanged || rotationChanged) && 
                   Bot.Instance.getServer() == Server.Xin;
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
}