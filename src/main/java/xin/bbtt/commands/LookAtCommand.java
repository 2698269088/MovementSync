package xin.bbtt.commands;

import xin.bbtt.mcbot.command.Command;

public class LookAtCommand extends Command {
    @Override
    public String getName() {
        return "look";
    }

    @Override
    public String[] getAliases() {
        return new String[]{"look"};
    }

    @Override
    public String getDescription() {
        return "A command to make bot look at a coordinate";
    }

    @Override
    public String getUsage() {
        return "look [x] [y] [z]";
    }
}
