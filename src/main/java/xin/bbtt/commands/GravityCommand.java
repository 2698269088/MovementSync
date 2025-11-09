package xin.bbtt.commands;

import xin.bbtt.mcbot.command.Command;

public class GravityCommand extends Command {
    
    @Override
    public String getName() {
        return "gravity";
    }
    
    @Override
    public String[] getAliases() {
        return new String[] {"gravity", "grav", "gravtoggle"};
    }
    
    @Override
    public String getDescription() {
        return "Toggle gravity for the bot";
    }
    
    @Override
    public String getUsage() {
        return "gravity [on|off]";
    }
}