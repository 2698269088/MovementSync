package xin.bbtt.commands;

import xin.bbtt.MovementSync;
import xin.bbtt.mcbot.command.Command;
import xin.bbtt.mcbot.command.CommandExecutor;

public class JumpCommandExecutor extends CommandExecutor {
    @Override
    public void onCommand(Command command, String s, String[] strings) {
        MovementSync.Instance.jump();
    }
}