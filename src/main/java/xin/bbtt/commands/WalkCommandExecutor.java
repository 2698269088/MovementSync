package xin.bbtt.commands;

import org.joml.Vector3d;
import xin.bbtt.MovementSync;
import xin.bbtt.mcbot.command.Command;
import xin.bbtt.mcbot.command.TabExecutor;
import xin.bbtt.world.Direction;

import java.util.List;

import static xin.bbtt.MovementSync.movementSpeed;

public class WalkCommandExecutor extends TabExecutor {
    @Override
    public void onCommand(Command command, String label, String[] args) {
        if (args.length == 0) return;
        if (args.length == 1) {
            args = new String[]{args[0], "20"};
        }
        Direction direction = Direction.valueOf(args[0]);
        double time = Double.parseDouble(args[1]) / 20;
        MovementSync.Instance.velocity.updateAndGet(p -> new Vector3d(p).add(direction.getVector(movementSpeed)));

    }

    @Override
    public List<String> onTabComplete(Command command, String label, String[] args) {
        if (args.length == 0) {
            return List.of("South", "North", "East", "West");
        }
        return List.of();
    }
}
