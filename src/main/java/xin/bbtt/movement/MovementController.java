package xin.bbtt.movement;

import xin.bbtt.MovementSync;
import xin.bbtt.tasks.MovementTask;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class MovementController {
    private final Queue<Movement> movements = new ConcurrentLinkedQueue<>();
    private final AtomicBoolean isExecuting = new AtomicBoolean(false);
    public final static MovementController Instance = new MovementController();

    private MovementController(){
    }

    public void addMovement(Movement movement) {
        movements.add(movement);
        tryExecuteNext();
    }

    public boolean hasMovement() {
        return !movements.isEmpty();
    }

    private void tryExecuteNext() {
        if (isExecuting.compareAndSet(false, true)) {
            doNext();
        }
    }

    public void doNext() {
        Movement movement = movements.poll();

        if (movement == null) {
            isExecuting.set(false);
            return;
        }

        try {
            movement.init();
            MovementTask task = new MovementTask(movement);

            ScheduledFuture<?> rateTaskFuture = MovementSync.Instance.movementService.scheduleAtFixedRate(
                    task,
                    0L,
                    50L,
                    TimeUnit.MILLISECONDS
            );
            MovementSync.Instance.movementService.schedule(() -> {
                try {
                    rateTaskFuture.cancel(true);
                    movement.onStop();
                } catch (Exception e) {
                    MovementSync.Instance.getLogger().error("Failed to stop the movement", e);
                } finally {
                    isExecuting.set(false);
                    tryExecuteNext();
                }
            }, movement.getTime(), TimeUnit.MILLISECONDS);

        } catch (Exception e) {
            MovementSync.Instance.getLogger().error("Failed to run the movement", e);
            isExecuting.set(false);
            tryExecuteNext();
        }
    }
}