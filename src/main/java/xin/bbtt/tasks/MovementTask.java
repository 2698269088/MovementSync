package xin.bbtt.tasks;

import xin.bbtt.MovementSync;
import xin.bbtt.movement.Movement;

public class MovementTask implements Runnable {
    public Movement movement;

    public MovementTask(Movement movement) {
        this.movement = movement;
    }

    @Override
    public void run() {
        if (Thread.currentThread().isInterrupted()) {
            return;
        }

        try {
            this.movement.onTick();
        } catch (Exception e) {
            MovementSync.Instance.getLogger().error("Failed to run onTick", e);
        }
    }
}