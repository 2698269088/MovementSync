package xin.bbtt.commands;

import org.joml.Vector3d;
import xin.bbtt.MovementSync;
import xin.bbtt.mcbot.command.Command;
import xin.bbtt.mcbot.command.TabExecutor;
import xin.bbtt.movements.WalkMovement;
import xin.bbtt.world.Direction;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class WalkCommandExecutor extends TabExecutor {
    @Override
    public void onCommand(Command command, String label, String[] args) {
        if (args.length == 0) return;
        if (args.length == 1) {
            args = new String[]{args[0], "1000"};
        }
        Vector3d velocity;
        switch (args[0]) {
            case "FRONT" ->
                    velocity = Direction.getUnitVectorByYaw(MovementSync.Instance.yaw.get()).mul(MovementSync.movementSpeed);
            case "LEFT" -> {
                Vector3d right = new Vector3d();
                Direction.getUnitVectorByYaw(MovementSync.Instance.yaw.get()).cross(Direction.UP.getUnitVector(), right).normalize();
                velocity = right.negate().mul(MovementSync.movementSpeed);
            }
            case "BACK" ->
                    velocity = Direction.getUnitVectorByYaw(MovementSync.Instance.yaw.get()).negate().mul(MovementSync.movementSpeed);
            case "RIGHT" -> {
                Vector3d right = new Vector3d();
                Direction.getUnitVectorByYaw(MovementSync.Instance.yaw.get()).cross(Direction.UP.getUnitVector(), right).normalize();
                velocity = right.mul(MovementSync.movementSpeed);
            }
            default -> {
                Direction direction = Direction.valueOf(args[0]);
                if (direction.getUnitVector().y() != 0) return;
                velocity = direction.getVector(MovementSync.movementSpeed);
            }
        }
        long time = Integer.parseInt(args[1]);
        MovementSync.Instance.movementController.addMovement(new WalkMovement(velocity, time));
    }

    @Override
    public List<String> onTabComplete(Command command, String label, String[] args) {
        if (args.length == 1) {
            List<String> directions = Arrays.stream(Direction.values())
                    .filter(d -> d.getUnitVector().y() == 0)
                    .map(Direction::toString)
                    .collect(Collectors.toList());
            directions.add("FRONT");
            directions.add("LEFT");
            directions.add("BACK");
            directions.add("RIGHT");
            return directions;
        }
        return List.of();
    }
}
