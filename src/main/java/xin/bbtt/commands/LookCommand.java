package xin.bbtt.commands;

import xin.bbtt.mcbot.command.Command;

public class LookCommand extends Command {
    @Override
    public String getName() {
        return "look";
    }

    @Override
    public String[] getAliases() {
        return new String[]{"look", "rotate", "yaw"};
    }

    @Override
    public String getDescription() {
        return "Set the bot's yaw rotation (0-360 degrees)";
    }

    @Override
    public String getUsage() {
        return "look <yaw>";
    }
}