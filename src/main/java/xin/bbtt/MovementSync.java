package xin.bbtt;

import org.geysermc.mcprotocollib.protocol.packet.ingame.serverbound.player.ServerboundMovePlayerPosPacket;
import org.joml.Vector3d;
import xin.bbtt.commands.*;
import xin.bbtt.listeners.*;
import xin.bbtt.move.MovementHelper;
import xin.bbtt.mcbot.Bot;
import xin.bbtt.mcbot.Server;
import xin.bbtt.mcbot.plugin.Plugin;
import xin.bbtt.world.World;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class MovementSync implements Plugin {
    public static MovementSync Instance;
    public int entityId = -1;

    // 线程安全的位置和速度
    public final AtomicReference<Vector3d> position = new AtomicReference<>(new Vector3d());
    public final AtomicReference<Vector3d> velocity = new AtomicReference<>(new Vector3d());

    // 物理常量
    public static final Vector3d GRAVITY = new Vector3d(0, -0.08, 0);
    public static final double DRAG_VERTICAL = 0.9800000190734863;
    public static final double DRAG_HORIZONTAL = 0.91;
    public static final double TERMINAL_VELOCITY = -3.92;

    public final AtomicBoolean onGround = new AtomicBoolean(true);

    // MCC移动系统
    private Queue<MoveCommandExecutor.MoveRequest> moveQueue = new ConcurrentLinkedQueue<>();
    private MoveCommandExecutor.MoveRequest currentMoveRequest = null;
    private int moveStepsCompleted = 0;

    public float yaw = 90.0f; // 默认朝东（90度），可根据需要调整
    private final AtomicReference<Vector3d> targetLook = new AtomicReference<>(new Vector3d());

    private Thread physicalSimulation;

    public MovementSync() {
        Instance = this;
    }

    @Override
    public void onLoad() {
        getLogger().info("Loading MovementSync");
    }

    @Override
    public void onUnload() {
        getLogger().info("Unloading MovementSync");
    }

    @Override
    public void onEnable() {
        getLogger().info("Enabling MovementSync");
        position.set(new Vector3d(0, 64, 0));
        velocity.set(new Vector3d());

        // 注册监听器
        Bot.Instance.addPacketListener(new TeleportPacketListener(), this);
        Bot.Instance.addPacketListener(new EntityIdRecorder(), this);
        Bot.Instance.addPacketListener(new RespawnPacketListener(), this);
        Bot.Instance.addPacketListener(new ChunkDataListener(), this);

        // 注册命令
        Bot.Instance.getPluginManager().registerCommand(new WhereAmICommand(), new WhereAmICommandExecutor(), this);
        Bot.Instance.getPluginManager().registerCommand(new JumpCommand(), new JumpCommandExecutor(), this);

        // 注册Look命令
        Bot.Instance.getPluginManager().registerCommand(new LookCommand(), new LookCommandExecutor(), this);

        MoveCommandExecutor moveExecutor = new MoveCommandExecutor();
        Bot.Instance.getPluginManager().registerCommand(new MoveCommand(), moveExecutor, this);
        this.moveQueue = moveExecutor.getMoveQueue();

        // 注册事件
        Bot.Instance.getPluginManager().events().registerEvents(new ServerChangeListener(), this);

        // 启动物理线程
        physicalSimulation = new Thread(this::mccPhysicsSimulation);
        physicalSimulation.start();
    }

    @Override
    public void onDisable() {
        getLogger().info("Disabling MovementSync");
        if (physicalSimulation != null) {
            physicalSimulation.interrupt();
        }
    }

    /**
     * MCC物理模拟主循环
     */
    private void mccPhysicsSimulation() {
        final long interval = 50_000_000L; // 20 TPS
        long nextTick = System.nanoTime();

        while (Bot.Instance.isRunning() && !Thread.currentThread().isInterrupted()) {
            if (Bot.Instance.getServer() != Server.Xin) {
                try { Thread.sleep(50); } catch (InterruptedException e) { break; }
                continue;
            }

            // 处理移动队列
            processMovementQueue();

            // 存储上一帧位置
            Vector3d lastPos = new Vector3d(position.get());

            // 更新物理状态
            updateMotionStateMCC();
            checkOnGroundMCC();

            // 同步到服务器
            if (!lastPos.equals(position.get()) && Bot.Instance.getServer() == Server.Xin) {
                syncPositionToServer();
            }

            // 精确控制帧率
            nextTick += interval;
            long sleepTime = (nextTick - System.nanoTime()) / 1_000_000L;
            if (sleepTime > 0) {
                try { Thread.sleep(sleepTime); } catch (InterruptedException e) { break; }
            } else {
                nextTick = System.nanoTime();
            }
        }
    }

    private void processMovementQueue() {
        // 如果当前没有移动任务，则从队列中获取一个
        if (currentMoveRequest == null) {
            currentMoveRequest = moveQueue.poll();
            moveStepsCompleted = 0;
            if (currentMoveRequest == null) return;
        }

        // 检查是否已完成所有步数
        if (moveStepsCompleted >= currentMoveRequest.steps) {
            getLogger().info("Finished moving {} steps in direction {}", currentMoveRequest.steps, currentMoveRequest.direction);
            currentMoveRequest = null;
            moveStepsCompleted = 0;
            // 停止移动
            Vector3d stopVelocity = velocity.get();
            stopVelocity.x = 0;
            stopVelocity.z = 0;
            velocity.set(stopVelocity);
            return;
        }

        // 设置移动速度，让bot朝目标位置移动
        Vector3d directionVector = currentMoveRequest.direction.getVector();
        Vector3d newVelocity = velocity.get();
        
        // 设置水平移动速度
        double moveSpeed = 0.2; // 基本移动速度
        newVelocity.x = directionVector.x * moveSpeed;
        newVelocity.z = directionVector.z * moveSpeed;
        
        // 如果是向上移动且在地面上，则跳跃
        if (directionVector.y > 0 && onGround.get()) {
            newVelocity.y = 0.42; // Minecraft跳跃初速度
            onGround.set(false);
            getLogger().info("Jumping!");
        }
        
        velocity.set(newVelocity);
        moveStepsCompleted++;
        
        getLogger().debug("Moving step {}/{} in direction {}", moveStepsCompleted, currentMoveRequest.steps, currentMoveRequest.direction);
    }

    /**
     * MCC重力算法
     */
    private void updateMotionStateMCC() {
        Vector3d vel = new Vector3d(velocity.get());
        Vector3d pos = new Vector3d(position.get());

        // 应用速度
        pos.add(vel);

        if (!onGround.get()) {
            // 垂直运动 - 重力影响
            vel.y -= 0.08;  // 重力加速度
            vel.y *= DRAG_VERTICAL;
            
            // 检查是否超过终端速度
            if (vel.y < TERMINAL_VELOCITY) {
                vel.y = TERMINAL_VELOCITY;
            }
        } else {
            // 地面上时重置垂直速度（除非正在跳跃）
            // 只有当没有主动设置y速度时才重置
            if (Math.abs(vel.y) < 0.001) {
                vel.y = 0;
            }
        }

        // 水平阻力（只有在没有主动移动时才应用）
        if (currentMoveRequest == null) {
            vel.x *= DRAG_HORIZONTAL;
            vel.z *= DRAG_HORIZONTAL;
            
            // 如果速度非常小，则置为0，避免无限微小移动
            if (Math.abs(vel.x) < 0.001) vel.x = 0;
            if (Math.abs(vel.z) < 0.001) vel.z = 0;
        }

        velocity.set(vel);
        position.set(pos);
    }

    /**
     * MCC地面检测
     */
    public void checkOnGroundMCC() {
        boolean grounded = MovementHelper.isOnGround(World.Instance, position.get());
        onGround.set(grounded);
    }

    public void syncPositionToServer() {
        Bot.Instance.getSession().send(new ServerboundMovePlayerPosPacket(
                onGround.get(), position.get().x, position.get().y, position.get().z
        ));

        getLogger().debug("Sync pos: ({}, {}, {}) onGround: {} velocity: ({}, {}, {})",
                position.get().x, position.get().y, position.get().z,
                onGround.get(), velocity.get().x, velocity.get().y, velocity.get().z
        );
    }

    public void jump() {
        // 添加强制跳跃功能，即使地面检测失败也能跳跃
        if (onGround.get()) {
            getLogger().info("Jumping!");
            onGround.set(false);
            Vector3d newVel = velocity.get();
            newVel.y = 0.42; // Minecraft跳跃初速度
            velocity.set(newVel);
        } else {
            // 尝试强制跳跃（解决地面检测问题）
            getLogger().info("Force jumping!");
            onGround.set(false);
            Vector3d newVel = velocity.get();
            newVel.y = 0.42; // Minecraft跳跃初速度
            velocity.set(newVel);
        }
    }

    public void setYaw(float newYaw) {
        this.yaw = newYaw % 360.0f;
        getLogger().info("Set bot yaw to {}", this.yaw);
    }

    public float getYaw() {
        return yaw;
    }
}