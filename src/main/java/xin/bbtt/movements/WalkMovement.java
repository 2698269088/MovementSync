package xin.bbtt.movements;

import org.joml.Vector3d;
import xin.bbtt.MovementSync;
import xin.bbtt.movement.Movement;
import xin.bbtt.world.Direction;

import static xin.bbtt.MovementSync.movementSpeed;

public class WalkMovement extends Movement {
    private final Direction direction;
    private final long time;

    public WalkMovement(Direction direction, long time){
        this.direction = direction;
        this.time = time;
    }

    @Override
    public void init() {
        MovementSync.Instance.velocity.updateAndGet(p -> new Vector3d(p).add(direction.getVector(movementSpeed)));
    }

    @Override
    public void onTick() {

    }

    @Override
    public long getTime() {
        return time;
    }

    @Override
    public void onStop() {
        MovementSync.Instance.velocity.updateAndGet(p -> new Vector3d(p).sub(direction.getVector(movementSpeed)));
    }
}
