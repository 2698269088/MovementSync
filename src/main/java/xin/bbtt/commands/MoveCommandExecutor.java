package xin.bbtt.commands;

import org.joml.Vector3d;
import xin.bbtt.MovementSync;
import xin.bbtt.mcbot.command.Command;
import xin.bbtt.mcbot.command.CommandExecutor;
import xin.bbtt.move.Direction;
import xin.bbtt.move.MovementController;
import xin.bbtt.move.Physics;

public class MoveCommandExecutor extends CommandExecutor {
    
    @Override
    public void onCommand(Command command, String s, String[] strings) {
        if (MovementSync.Instance == null) {
            System.out.println("MovementSync instance not available");
            return;
        }
        
        if (strings.length < 1) {
            MovementSync.Instance.getLogger().info("Usage: move <direction> [distance]");
            MovementSync.Instance.getLogger().info("Directions: up, down, north, south, east, west");
            return;
        }
        
        String directionStr = strings[0].toLowerCase();
        double distance = 1.0; // 默认移动距离
        
        if (strings.length >= 2) {
            try {
                distance = Double.parseDouble(strings[1]);
            } catch (NumberFormatException e) {
                MovementSync.Instance.getLogger().info("Invalid distance: {}", strings[1]);
                return;
            }
        }
        
        Direction direction = parseDirection(directionStr);
        if (direction == null) {
            MovementSync.Instance.getLogger().info("Unknown direction: {}", directionStr);
            MovementSync.Instance.getLogger().info("Valid directions: up, down, north, south, east, west");
            return;
        }
        
        MovementController controller = MovementSync.Instance.getMovementController();
        Vector3d currentPosition = controller.getPosition();
        Vector3d targetPosition = new Vector3d(
            currentPosition.x + direction.x * distance,
            currentPosition.y + direction.y * distance,
            currentPosition.z + direction.z * distance
        );
        
        // 尝试移动
        if (controller.moveTo(MovementSync.Instance.getWorld(), targetPosition)) {
            MovementSync.Instance.getLogger().info("Moved {} blocks {}", distance, directionStr);
        } else {
            MovementSync.Instance.getLogger().info("Cannot move {} blocks {}", distance, directionStr);
        }
    }
    
    private Direction parseDirection(String directionStr) {
        return switch (directionStr) {
            case "up" -> Direction.UP;
            case "down" -> Direction.DOWN;
            case "north" -> Direction.NORTH;
            case "south" -> Direction.SOUTH;
            case "west" -> Direction.WEST;
            case "east" -> Direction.EAST;
            default -> null;
        };
    }
}