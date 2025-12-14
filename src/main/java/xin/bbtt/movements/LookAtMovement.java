package xin.bbtt.movements;

import org.joml.Vector3d;
import org.joml.Vector3i;
import xin.bbtt.MovementSync;
import xin.bbtt.movement.Movement;
import xin.bbtt.world.Direction;


public class LookAtMovement extends Movement {
    public final Vector3d target;

    public LookAtMovement(Vector3d target) {
        this.target = target;
    }

    public LookAtMovement(Vector3i target) {
        this.target = new Vector3d(
                target.x + .5d,
                target.y + .5d,
                target.z + .5d
        );
    }

    @Override
    public void init() {
        Vector3d headPosition = new Vector3d(MovementSync.Instance.position.get())
                .add(Direction.UP.getVector(1.62));

        Vector3d delta = new Vector3d(target).sub(headPosition);

        double dx = delta.x;
        double dy = delta.y;
        double dz = delta.z;

        float yaw = (float) Math.toDegrees(Math.atan2(dz, dx)) - 90;
        float pitch = (float) Math.toDegrees(-Math.atan2(dy, Math.sqrt(dx*dx + dz*dz)));

        MovementSync.Instance.yaw.set(yaw);
        MovementSync.Instance.pitch.set(pitch);
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
