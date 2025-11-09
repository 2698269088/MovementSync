package xin.bbtt.commands;

import xin.bbtt.MovementSync;
import xin.bbtt.mcbot.command.Command;
import xin.bbtt.mcbot.command.CommandExecutor;
import xin.bbtt.move.MovementController;
import xin.bbtt.move.Physics;

public class JumpCommandExecutor extends CommandExecutor {
    
    @Override
    public void onCommand(Command command, String s, String[] strings) {
        if (MovementSync.Instance == null) {
            System.out.println("MovementSync instance not available");
            return;
        }
        
        MovementController controller = MovementSync.Instance.getMovementController();
        
        // 检查是否在地面上才能跳跃
        if (Physics.isOnGround(MovementSync.Instance.getWorld(), controller.getPosition())) {
            // 应用向上的动量
            controller.jump();
            MovementSync.Instance.getLogger().info("Bot jumped!");
        } else {
            MovementSync.Instance.getLogger().info("Cannot jump while not on ground");
        }
    }
}