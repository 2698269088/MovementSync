package xin.bbtt.commands;

import xin.bbtt.mcbot.command.Command;

public class MoveCommand extends Command {
    
    @Override
    public String getName() {
        return "move";
    }
    
    @Override
    public String[] getAliases() {
        return new String[] {"move", "mv", "walk"};
    }
    
    @Override
    public String getDescription() {
        return "Move the bot in a specified direction";
    }
    
    @Override
    public String getUsage() {
        return "move <direction> [distance]";
    }
}