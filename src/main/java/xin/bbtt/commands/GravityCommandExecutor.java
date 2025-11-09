package xin.bbtt.commands;

import xin.bbtt.MovementSync;
import xin.bbtt.mcbot.command.Command;
import xin.bbtt.mcbot.command.CommandExecutor;
import xin.bbtt.move.MovementController;
import xin.bbtt.move.Physics;

public class GravityCommandExecutor extends CommandExecutor {
    
    @Override
    public void onCommand(Command command, String s, String[] strings) {
        if (MovementSync.Instance == null) {
            System.out.println("MovementSync instance not available");
            return;
        }
        
        // 获取移动控制器实例
        MovementController controller = MovementSync.Instance.getMovementController();
        
        if (strings.length == 0) {
            // 切换重力状态
            boolean newGravityState = !controller.isGravityEnabled();
            controller.setGravityEnabled(newGravityState);
            MovementSync.Instance.getLogger().info("Gravity {}", newGravityState ? "enabled" : "disabled");
        } else if (strings.length >= 1) {
            String state = strings[0].toLowerCase();
            switch (state) {
                case "on":
                case "true":
                case "1":
                    controller.setGravityEnabled(true);
                    MovementSync.Instance.getLogger().info("Gravity enabled");
                    break;
                case "off":
                case "false":
                case "0":
                    controller.setGravityEnabled(false);
                    MovementSync.Instance.getLogger().info("Gravity disabled");
                    break;
                default:
                    MovementSync.Instance.getLogger().info("Usage: gravity [on|off]");
                    break;
            }
        }
    }
}