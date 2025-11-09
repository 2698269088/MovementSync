package xin.bbtt.move;

import org.geysermc.mcprotocollib.network.Session;
import org.geysermc.mcprotocollib.protocol.packet.ingame.serverbound.player.ServerboundMovePlayerPosPacket;
import org.joml.Vector3d;
import xin.bbtt.MovementSync;
import xin.bbtt.world.World;

public class MovementController {
    
    // 当前位置
    private Vector3d position;
    
    // 当前动量
    private double motionX = 0;
    private double motionY = 0;
    private double motionZ = 0;

    // 是否启用重力
    private boolean gravityEnabled = true;
    
    // 用于跟踪上一次发送的位置，避免重复发送相同位置
    private Vector3d lastSentPosition = null;
    
    public MovementController() {
        this.position = new Vector3d(0, 0, 0);
        this.lastSentPosition = new Vector3d(0, 0, 0);
    }
    
    public MovementController(Vector3d initialPosition) {
        this.position = new Vector3d(initialPosition);
        this.lastSentPosition = new Vector3d(initialPosition);
    }
    
    /**
     * 更新物理状态
     * @param world 当前世界
     */
    public void updatePhysics(World world) {
        Vector3d oldPosition = new Vector3d(this.position);
        
        // 应用水平动量
        position.x += motionX;
        position.z += motionZ;
        
        // 应用空气阻力到水平动量
        motionX *= 0.91;
        motionZ *= 0.91;
        
        // 如果动量非常小，将其置零
        if (Math.abs(motionX) < 0.01) motionX = 0;
        if (Math.abs(motionZ) < 0.01) motionZ = 0;
        
        if (gravityEnabled) {
            Physics.PhysicsResult result = Physics.handleGravity(world, position, motionY);
            this.position = result.position;
            this.motionY = result.motionY;
        }
        
        // 如果位置发生变化，发送更新到服务器
        // 使用距离比较而不是对象比较，避免浮点数精度问题
        if (oldPosition.distance(position) > 0.001) {
            sendPositionUpdate();
        }
    }
    
    /**
     * 移动到指定位置
     * @param world 当前世界
     * @param targetPosition 目标位置
     * @return 是否成功移动
     */
    public boolean moveTo(World world, Vector3d targetPosition) {
        if (Physics.canMoveTo(world, position, targetPosition)) {
            this.position = targetPosition;
            sendPositionUpdate();
            return true;
        }
        return false;
    }
    
    /**
     * 分步移动到指定位置
     * @param world 当前世界
     * @param targetPosition 目标位置
     * @param stepsByBlock 每个方块的步数
     * @return 移动步骤数组
     */
    public Vector3d[] moveToInSteps(World world, Vector3d targetPosition, int stepsByBlock) {
        return Physics.moveInSteps(position, targetPosition, motionY, !Physics.isOnGround(world, position), stepsByBlock);
    }
    
    /**
     * 设置位置
     * @param position 新位置
     */
    public void setPosition(Vector3d position) {
        this.position = position;
    }
    
    /**
     * 获取当前位置
     * @return 当前位置
     */
    public Vector3d getPosition() {
        return new Vector3d(position);
    }
    
    /**
     * 设置动量
     * @param motionX X轴动量
     * @param motionY Y轴动量
     * @param motionZ Z轴动量
     */
    public void setMotion(double motionX, double motionY, double motionZ) {
        this.motionX = motionX;
        this.motionY = motionY;
        this.motionZ = motionZ;
    }
    
    /**
     * 获取X轴动量
     * @return X轴动量
     */
    public double getMotionX() {
        return motionX;
    }
    
    /**
     * 获取Y轴动量
     * @return Y轴动量
     */
    public double getMotionY() {
        return motionY;
    }
    
    /**
     * 获取Z轴动量
     * @return Z轴动量
     */
    public double getMotionZ() {
        return motionZ;
    }
    
    /**
     * 应用跳跃动量
     */
    public void jump() {
        if (Physics.isOnGround(MovementSync.Instance.getWorld(), position)) {
            this.motionY = 0.42F; // Minecraft中玩家跳跃的初始动量
            sendPositionUpdate();
        }
    }

    /**
     * 启用或禁用重力
     * @param enabled 是否启用
     */
    public void setGravityEnabled(boolean enabled) {
        this.gravityEnabled = enabled;
    }

    /**
     * 检查重力是否启用
     * @return 重力是否启用
     */
    public boolean isGravityEnabled() {
        return gravityEnabled;
    }

    /**
     * 发送位置更新到服务器
     */
    private void sendPositionUpdate() {
        // 检查位置是否与上次发送的位置相同，避免重复发送
        if (lastSentPosition != null && 
            lastSentPosition.distance(position) < 0.01) {
            return; // 位置变化太小，不发送更新
        }
        
        try {
            if (MovementSync.Instance != null) {
                Session session = MovementSync.Instance.getSession();
                if (session != null) {
                    // 直接创建并发送数据包
                    // 正确的参数顺序是 onGround, x, y, z
                    ServerboundMovePlayerPosPacket packet = new ServerboundMovePlayerPosPacket(
                            true, position.x, position.y, position.z);
                    session.send(packet);
                    
                    // 更新最后发送的位置
                    if (lastSentPosition == null) {
                        lastSentPosition = new Vector3d(position);
                    } else {
                        lastSentPosition.set(position);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to send position update packet: " + e.getMessage());
            e.printStackTrace();
        }
    }

}