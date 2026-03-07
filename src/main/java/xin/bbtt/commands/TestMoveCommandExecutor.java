package xin.bbtt.commands;

import xin.bbtt.MovementSync;
import xin.bbtt.mcbot.command.Command;
import xin.bbtt.mcbot.command.CommandExecutor;
import xin.bbtt.tasks.VanillaPhysicsTask;

/**
 * 测试移动命令执行器
 */
public class TestMoveCommandExecutor extends CommandExecutor {
    @Override
    public void onCommand(Command command, String label, String[] args) {
        if (MovementSync.Instance == null) {
            MovementSync.Instance.getLogger().error("MovementSync 未初始化！");
            return;
        }
        
        if (MovementSync.Instance.position.get() == null) {
            MovementSync.Instance.getLogger().error("位置数据为空！");
            return;
        }
        
        MovementSync.Instance.getLogger().info("=== 移动测试开始 ===");
        MovementSync.Instance.getLogger().info("当前位置：" + 
            String.format("%.4f, %.4f, %.4f", 
                MovementSync.Instance.position.get().x,
                MovementSync.Instance.position.get().y,
                MovementSync.Instance.position.get().z
            )
        );
        MovementSync.Instance.getLogger().info("当前角度：yaw=" + 
            String.format("%.2f", MovementSync.Instance.yaw.get()) + 
            ", pitch=" + String.format("%.2f", MovementSync.Instance.pitch.get())
        );
        MovementSync.Instance.getLogger().info("在地面：" + MovementSync.Instance.onGround.get());
        
        // 直接设置一个向前的速度来测试
        MovementSync.Instance.getLogger().info("尝试向前移动...");
        VanillaPhysicsTask.setMovementInput(0, 1.0); // 向前输入
        
        MovementSync.Instance.getLogger().info("已设置移动输入，请观察日志中的移动包输出");
        MovementSync.Instance.getLogger().info("如果看到'发送移动包'日志，说明包已成功发送");
        MovementSync.Instance.getLogger().info("=== 测试命令执行完毕 ===");
    }
}
