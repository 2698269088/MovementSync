package xin.bbtt.commands;

import xin.bbtt.MovementSync;
import xin.bbtt.mcbot.Bot;
import xin.bbtt.mcbot.command.Command;
import xin.bbtt.mcbot.command.CommandExecutor;

public class WhereAmICommandExecutor extends CommandExecutor {
    @Override
    public void onCommand(Command command, String s, String[] strings) {
        MovementSync.Instance.getLogger().info("You're in {} at ({}, {}, {})", Bot.Instance.getServer(),MovementSync.position.x, MovementSync.position.y, MovementSync.position.z);
    }
}
