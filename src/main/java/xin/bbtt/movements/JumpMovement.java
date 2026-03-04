package xin.bbtt.movements;

import org.joml.Vector3d;
import xin.bbtt.MovementSync;
import xin.bbtt.movement.Movement;

public class JumpMovement extends Movement {
    @Override
    public void init() {
        // 原版MC跳跃条件检查
        if (MovementSync.Instance.onGround.get() && 
            MovementSync.Instance.velocity != null) {
            
            Vector3d currentVelocity = MovementSync.Instance.velocity.get();
            // 原版跳跃动力学：只有当垂直速度接近0时才能跳跃
            if (Math.abs(currentVelocity.y) < 0.1) {
                MovementSync.Instance.getLogger().info("jumping with vanilla physics");
                // 使用原版标准跳跃动力 0.42
                MovementSync.Instance.velocity.updateAndGet(p -> new Vector3d(p).add(new Vector3d(0, 0.42, 0)));
                // 设置离地状态
                MovementSync.Instance.onGround.set(false);
            }
        }
    }

    @Override
    public void onTick() {

    }

    @Override
    public long getTime() {
        return 0;
    }

    @Override
    public void onStop() {
    }
}
