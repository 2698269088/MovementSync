package xin.bbtt.commands;

import org.joml.Vector3d;
import xin.bbtt.MovementSync;
import xin.bbtt.mcbot.Bot;
import xin.bbtt.mcbot.command.Command;
import xin.bbtt.mcbot.command.CommandExecutor;
import xin.bbtt.move.Direction;
import xin.bbtt.move.MovementHelper;
import xin.bbtt.world.World;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * MCC风格的移动命令执行器
 * 支持队列移动和方向解析
 */
public class MoveCommandExecutor extends CommandExecutor {
    private final Queue<MoveRequest> moveQueue = new ConcurrentLinkedQueue<>();

    @Override
    public void onCommand(Command command, String label, String[] args) {
        if (args.length < 1) {
            MovementSync.Instance.getLogger().info("Usage: " + command.getUsage());
            return;
        }

        String directionStr = args[0].toLowerCase();
        int steps = 1;

        if (args.length > 1) {
            try {
                steps = Integer.parseInt(args[1]);
                if (steps <= 0) steps = 1;
            } catch (NumberFormatException e) {
                MovementSync.Instance.getLogger().info("Invalid steps number!");
                return;
            }
        }

        Direction direction = parseDirection(directionStr);
        if (direction == null) {
            MovementSync.Instance.getLogger().info("Invalid direction! Use: forward, back, left, right, up");
            return;
        }

        // 不再检查是否可以移动，直接添加到队列中
        // MovementHelper.canMove检查有时会误判，我们让物理系统来处理

        moveQueue.offer(new MoveRequest(direction, steps));
        MovementSync.Instance.getLogger().info("Queued {} steps to {}", steps, directionStr);
    }

    private Direction parseDirection(String str) {
        return switch (str) {
            case "forward", "w", "f" -> getForwardDirection();
            case "back", "s", "b" -> getBackDirection();
            case "left", "a", "l" -> getLeftDirection();
            case "right", "d", "r" -> getRightDirection();
            case "up", "jump" -> Direction.Up; // 添加向上/跳跃指令
            default -> null;
        };
    }

    private Direction getForwardDirection() {
        float yaw = MovementSync.Instance.getYaw();
        double rad = Math.toRadians(yaw);
        if (Math.abs(Math.sin(rad)) > 0.707) {
            return Math.cos(rad) > 0 ? Direction.South : Direction.North;
        } else {
            return Math.sin(rad) > 0 ? Direction.West : Direction.East;
        }
    }

    private Direction getBackDirection() {
        float yaw = MovementSync.Instance.getYaw();
        double rad = Math.toRadians(yaw);
        if (Math.abs(Math.sin(rad)) > 0.707) {
            return Math.cos(rad) > 0 ? Direction.North : Direction.South;
        } else {
            return Math.sin(rad) > 0 ? Direction.East : Direction.West;
        }
    }

    private Direction getLeftDirection() {
        float yaw = MovementSync.Instance.getYaw();
        double rad = Math.toRadians(yaw);
        if (Math.abs(Math.sin(rad)) > 0.707) {
            return Math.cos(rad) > 0 ? Direction.East : Direction.West;
        } else {
            return Math.sin(rad) > 0 ? Direction.South : Direction.North;
        }
    }

    private Direction getRightDirection() {
        float yaw = MovementSync.Instance.getYaw();
        double rad = Math.toRadians(yaw);
        if (Math.abs(Math.sin(rad)) > 0.707) {
            return Math.cos(rad) > 0 ? Direction.West : Direction.East;
        } else {
            return Math.sin(rad) > 0 ? Direction.North : Direction.South;
        }
    }

    public Queue<MoveRequest> getMoveQueue() {
        return moveQueue;
    }

    public static class MoveRequest {
        public final Direction direction;
        public final int steps;

        public MoveRequest(Direction direction, int steps) {
            this.direction = direction;
            this.steps = steps;
        }
    }
}