package xin.bbtt.movements;

import org.geysermc.mcprotocollib.protocol.packet.ingame.serverbound.player.ServerboundMovePlayerPosRotPacket;
import org.joml.Vector3d;
import xin.bbtt.MovementSync;
import xin.bbtt.movement.Movement;
import xin.bbtt.mcbot.Bot;

public class WalkMovement extends Movement {
    private final Vector3d velocity;
    private final long time;
    private long elapsed = 0;
    private Vector3d lastSentPosition = new Vector3d();

    public WalkMovement(Vector3d velocity, long time){
        this.velocity = velocity;
        this.time = time;
    }

    @Override
    public void init() {
        MovementSync.Instance.velocity.updateAndGet(p -> new Vector3d(p).add(velocity));
        lastSentPosition = new Vector3d(MovementSync.Instance.position.get());
    }

    @Override
    public void onTick() {
        // 每 tick 更新位置（50ms）
        elapsed += 50;
        
        // 应用速度到位置 - 参考 MCC 的移动方式
        // MCC 使用更快的速度，而不是浮空速度
        Vector3d currentPos = new Vector3d(MovementSync.Instance.position.get());
        
        // MCC 的移动速度：每 tick 约 0.2-0.3 个方块（正常步行速度）
        // 你的速度太慢是因为使用了 velocity * 0.05，这只有浮空速度
        double moveSpeed = 0.2; // MCC 标准步行速度
        Vector3d moveDirection = new Vector3d(velocity).normalize();
        Vector3d newPos = new Vector3d(currentPos).add(
            moveDirection.x * moveSpeed,
            velocity.y * 0.05,  // Y 轴保持较慢速度
            moveDirection.z * moveSpeed
        );
        
        // 更新物理模拟的位置
        MovementSync.Instance.position.set(newPos);
        
        // 参考 MCC 的做法：直接发送位置更新到服务器
        // 不像原版物理那样等待阈值，而是每 tick 都发送
        sendPositionToServer();
        
        // 更新最后发送的位置
        lastSentPosition = new Vector3d(newPos);
    }

    /**
     * 直接发送位置到服务器 - 参考 MCC 的即时同步方式
     */
    private void sendPositionToServer() {
        if (Bot.Instance.getSession() != null && MovementSync.Instance != null) {
            try {
                Vector3d pos = MovementSync.Instance.position.get();
                if (pos != null) {
                    ServerboundMovePlayerPosRotPacket packet = new ServerboundMovePlayerPosRotPacket(
                        MovementSync.Instance.onGround.get(),
                        pos.x,
                        pos.y,
                        pos.z,
                        MovementSync.Instance.yaw.get(),
                        MovementSync.Instance.pitch.get()
                    );
                    Bot.Instance.getSession().send(packet);
                    
                    // 每 10 tick 输出一次调试信息
                    if (elapsed % 500 == 0 && MovementSync.Instance.getLogger().isDebugEnabled()) {
                        MovementSync.Instance.getLogger().debug(
                            String.format("WalkMovement - 已发送位置更新：%.4f, %.4f, %.4f | 速度：%.4f, %.4f, %.4f",
                                pos.x, pos.y, pos.z,
                                velocity.x, velocity.y, velocity.z
                            )
                        );
                    }
                }
            } catch (Exception e) {
                MovementSync.Instance.getLogger().error("Failed to send position packet", e);
            }
        }
    }

    @Override
    public long getTime() {
        return time;
    }

    @Override
    public void onStop() {
        MovementSync.Instance.velocity.updateAndGet(p -> new Vector3d(p).sub(velocity));
    }
}
