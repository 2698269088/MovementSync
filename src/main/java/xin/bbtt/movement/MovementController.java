package xin.bbtt.movement;

import xin.bbtt.MovementSync;
import xin.bbtt.tasks.MovementTask;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class MovementController {
    private final Queue<Movement> movements = new ConcurrentLinkedQueue<>();
    private final AtomicBoolean isExecuting = new AtomicBoolean(false);
    private ScheduledFuture<?> currentTaskFuture = null;
    private ScheduledFuture<?> currentStopFuture = null;
    private Movement currentMovement = null;

    public void addMovement(Movement movement) {
        movements.add(movement);
        tryExecuteNext();
    }

    public void cancelAll() {
        movements.clear();

        if (currentTaskFuture != null) {
            currentTaskFuture.cancel(true);
        }
        if (currentStopFuture != null) {
            currentStopFuture.cancel(true);
        }

        if (currentMovement != null) {
            try {
                currentMovement.onStop();
            } catch (Exception e) {
                MovementSync.Instance.getLogger().error("Failed to stop the movement", e);
            }
            currentMovement = null;
        }

        isExecuting.set(false);
        currentTaskFuture = null;
        currentStopFuture = null;
    }


    @SuppressWarnings("unused")
    public boolean hasMovement() {
        return !movements.isEmpty();
    }

    private void tryExecuteNext() {
        if (isExecuting.compareAndSet(false, true)) {
            doNext();
        }
    }

    public void doNext() {
        currentMovement = movements.poll();

        if (currentMovement == null) {
            isExecuting.set(false);
            return;
        }

        if (MovementSync.Instance.movementService == null) {
            MovementSync.Instance.movementService = Executors.newScheduledThreadPool(1);
        }

        try {
            currentMovement.init();
            MovementTask task = new MovementTask(currentMovement);

            currentTaskFuture = MovementSync.Instance.movementService.scheduleAtFixedRate(
                    task,
                    0L,
                    50L,
                    TimeUnit.MILLISECONDS
            );

            currentStopFuture = MovementSync.Instance.movementService.schedule(() -> {
                try {
                    currentTaskFuture.cancel(true);
                    currentMovement.onStop();
                } catch (Exception e) {
                    MovementSync.Instance.getLogger().error("Failed to stop the movement", e);
                } finally {
                    isExecuting.set(false);
                    currentTaskFuture = null;
                    currentStopFuture = null;
                    tryExecuteNext();
                }
            }, currentMovement.getTime(), TimeUnit.MILLISECONDS);

        } catch (Exception e) {
            MovementSync.Instance.getLogger().error("Failed to run the movement", e);
            isExecuting.set(false);
            tryExecuteNext();
        }
    }
}
