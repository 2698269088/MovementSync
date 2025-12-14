package xin.bbtt.commands;

import xin.bbtt.mcbot.command.Command;
import xin.bbtt.mcbot.command.TabExecutor;
import xin.bbtt.movement.MovementController;
import xin.bbtt.movements.WalkMovement;
import xin.bbtt.world.Direction;

import java.util.Arrays;
import java.util.List;

public class WalkCommandExecutor extends TabExecutor {
    @Override
    public void onCommand(Command command, String label, String[] args) {
        if (args.length == 0) return;
        if (args.length == 1) {
            args = new String[]{args[0], "1000"};
        }
        Direction direction = Direction.valueOf(args[0]);
        long time = Integer.parseInt(args[1]);

        MovementController.Instance.addMovement(new WalkMovement(direction, time));
    }

    @Override
    public List<String> onTabComplete(Command command, String label, String[] args) {
        if (args.length == 1) {
            return Arrays.stream(Direction.values()).filter(direction->direction.getUnitVector().y() == 0).map(Direction::toString).toList();
        }
        return List.of();
    }
}
