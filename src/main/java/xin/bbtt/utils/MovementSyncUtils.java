package xin.bbtt.utils;

import org.joml.Vector3d;
import xin.bbtt.MovementSync;

/**
 * MovementSync安全访问工具类
 * 提供空值检查和安全的方法调用
 */
public class MovementSyncUtils {
    
    /**
     * 安全地获取当前位置
     * @return 当前位置，如果不可用则返回原点
     */
    public static Vector3d getCurrentPosition() {
        if (MovementSync.Instance != null && 
            MovementSync.Instance.position != null && 
            MovementSync.Instance.position.get() != null) {
            return new Vector3d(MovementSync.Instance.position.get());
        }
        return new Vector3d(0, 0, 0);
    }
    
    /**
     * 安全地获取当前速度
     * @return 当前速度，如果不可用则返回零向量
     */
    public static Vector3d getCurrentVelocity() {
        if (MovementSync.Instance != null && 
            MovementSync.Instance.velocity != null && 
            MovementSync.Instance.velocity.get() != null) {
            return new Vector3d(MovementSync.Instance.velocity.get());
        }
        return new Vector3d(0, 0, 0);
    }
    
    /**
     * 安全地获取是否在地面上
     * @return 是否在地面，如果不可用则返回true（安全默认值）
     */
    public static boolean isOnGround() {
        if (MovementSync.Instance != null && 
            MovementSync.Instance.onGround != null) {
            return MovementSync.Instance.onGround.get();
        }
        return true; // 安全默认值
    }
    
    /**
     * 安全地获取当前俯仰角
     * @return 当前俯仰角，如果不可用则返回0
     */
    public static float getCurrentPitch() {
        if (MovementSync.Instance != null && 
            MovementSync.Instance.pitch != null && 
            MovementSync.Instance.pitch.get() != null) {
            return MovementSync.Instance.pitch.get();
        }
        return 0f;
    }
    
    /**
     * 安全地获取当前偏航角
     * @return 当前偏航角，如果不可用则返回0
     */
    public static float getCurrentYaw() {
        if (MovementSync.Instance != null && 
            MovementSync.Instance.yaw != null && 
            MovementSync.Instance.yaw.get() != null) {
            return MovementSync.Instance.yaw.get();
        }
        return 0f;
    }
    
    /**
     * 安全地触发跳跃
     */
    public static void jump() {
        if (MovementSync.Instance != null) {
            MovementSync.Instance.jump();
        }
    }
    
    /**
     * 安全地设置看向目标
     * @param target 目标位置
     */
    public static void lookAt(Vector3d target) {
        if (MovementSync.Instance != null && target != null) {
            MovementSync.Instance.lookAt(target);
        }
    }
    
    /**
     * 安全地获取头部位置
     * @return 头部位置，如果不可用则返回默认值
     */
    public static Vector3d getHeadPosition() {
        if (MovementSync.Instance != null) {
            return MovementSync.Instance.getHeadPosition();
        }
        return new Vector3d(0, 1.62, 0); // 默认头部高度
    }
    
    /**
     * 检查MovementSync是否已正确初始化
     * @return 是否初始化完成
     */
    public static boolean isInitialized() {
        return MovementSync.Instance != null &&
               MovementSync.Instance.position != null &&
               MovementSync.Instance.velocity != null &&
               MovementSync.Instance.pitch != null &&
               MovementSync.Instance.yaw != null;
    }
}