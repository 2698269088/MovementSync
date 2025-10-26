package xin.bbtt.commands;

import xin.bbtt.mcbot.command.Command;

public class WhereAmICommand extends Command {

    @Override
    public String getName() {
        return "whereami";
    }

    @Override
    public String[] getAliases() {
        return new String[] {"whereami"};
    }

    @Override
    public String getDescription() {
        return "Show bot's position";
    }

    @Override
    public String getUsage() {
        return "whereami";
    }
}
