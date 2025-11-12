package xin.bbtt.commands;

import xin.bbtt.MovementSync;
import xin.bbtt.mcbot.command.Command;
import xin.bbtt.mcbot.command.CommandExecutor;

public class LookCommandExecutor extends CommandExecutor {
    @Override
    public void onCommand(Command command, String s, String[] args) {
        if (args.length < 1) {
            MovementSync.Instance.getLogger().info("Current yaw: {}", MovementSync.Instance.getYaw());
            MovementSync.Instance.getLogger().info("Usage: {}", command.getUsage());
            return;
        }

        try {
            float yaw = Float.parseFloat(args[0]);
            MovementSync.Instance.setYaw(yaw);
        } catch (NumberFormatException e) {
            MovementSync.Instance.getLogger().info("Invalid yaw value!");
        }
    }
}