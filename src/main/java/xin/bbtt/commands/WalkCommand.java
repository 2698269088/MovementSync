package xin.bbtt.commands;

import xin.bbtt.mcbot.command.Command;

public class WalkCommand extends Command {
    @Override
    public String getName() {
        return "walk";
    }

    @Override
    public String[] getAliases() {
        return new String[]{"walk"};
    }

    @Override
    public String getDescription() {
        return "A command to walk";
    }

    @Override
    public String getUsage() {
        return "walk direction time(milliseconds)";
    }
}
