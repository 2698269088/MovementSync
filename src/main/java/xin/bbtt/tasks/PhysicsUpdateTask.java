package xin.bbtt.tasks;

import xin.bbtt.MovementSync;
import xin.bbtt.move.MovementController;

/**
 * 物理更新任务，定期更新机器人的物理状态
 */
public class PhysicsUpdateTask implements Runnable {
    
    private final MovementSync plugin;
    private boolean running = false;
    
    public PhysicsUpdateTask(MovementSync plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public void run() {
        if (!running) {
            return;
        }
        
        try {
            // 获取移动控制器
            MovementController controller = plugin.getMovementController();
            
            // 更新物理状态
            controller.updatePhysics(plugin.getWorld());
            
            // 更新全局位置变量
            MovementSync.position = controller.getPosition();
            
        } catch (Exception e) {
            plugin.getLogger().error("Error updating physics: {}", e.getMessage());
        }
    }
    
    public void start() {
        this.running = true;
    }
    
    public void stop() {
        this.running = false;
    }
    
    public boolean isRunning() {
        return running;
    }
}