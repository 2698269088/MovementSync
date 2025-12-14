package xin.bbtt.commands;

import xin.bbtt.mcbot.command.Command;

public class GetBlockAtCommand extends Command {
    @Override
    public String getName() {
        return "block";
    }

    @Override
    public String[] getAliases() {
        return new String[]{"block"};
    }

    @Override
    public String getDescription() {
        return "A command to get the block at the specific coordinate.";
    }

    @Override
    public String getUsage() {
        return "block [x] [y] [z]";
    }
}
