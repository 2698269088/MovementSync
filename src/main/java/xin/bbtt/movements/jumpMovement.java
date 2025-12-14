package xin.bbtt.movements;

import org.joml.Vector3d;
import xin.bbtt.MovementSync;
import xin.bbtt.movement.Movement;

public class jumpMovement extends Movement {
    @Override
    public void init() {
        if (MovementSync.Instance.onGround.get()) {
            MovementSync.Instance.getLogger().info("jumping");
            MovementSync.Instance.isJumping.set(true);
            MovementSync.Instance.velocity.updateAndGet(p -> new Vector3d(p).add(new Vector3d(0, 0.42, 0)));
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
        MovementSync.Instance.isJumping.set(false);
    }
}
