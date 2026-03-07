package xin.bbtt.commands;

import xin.bbtt.mcbot.command.Command;

/**
 * 测试移动命令
 * 用于验证移动包是否能成功发送到服务器
 */
public class TestMoveCommand extends Command {
    @Override
    public String getName() {
        return "testmove";
    }

    @Override
    public String[] getAliases() {
        return new String[] {"testmove"};
    }

    @Override
    public String getDescription() {
        return "Test movement packet sending";
    }

    @Override
    public String getUsage() {
        return "testmove";
    }
}
