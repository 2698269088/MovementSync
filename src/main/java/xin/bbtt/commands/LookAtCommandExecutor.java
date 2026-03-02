package xin.bbtt.commands;

import org.joml.Vector3d;
import xin.bbtt.MovementSync;
import xin.bbtt.mcbot.command.Command;
import xin.bbtt.mcbot.command.CommandExecutor;

public class LookAtCommandExecutor extends CommandExecutor {
    @Override
    public void onCommand(Command command, String s, String[] args) {
        if (args.length != 3) return;
        double x = Double.parseDouble(args[0]);
        double y = Double.parseDouble(args[1]);
        double z = Double.parseDouble(args[2]);
        Vector3d target = new Vector3d(x, y, z);
        MovementSync.Instance.lookAt(target);
    }
}
