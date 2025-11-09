package xin.bbtt;

import org.geysermc.mcprotocollib.network.Session;
import org.joml.Vector3d;
import xin.bbtt.commands.*;
import xin.bbtt.listeners.*;
import xin.bbtt.mcbot.Bot;
import xin.bbtt.mcbot.plugin.Plugin;
import xin.bbtt.move.MovementController;
import xin.bbtt.tasks.PhysicsUpdateTask;
import xin.bbtt.world.World;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MovementSync implements Plugin {
    public static MovementSync Instance;
    public static int entityId = -1;
    public static Vector3d position;
    
    // 添加移动控制器
    private MovementController movementController;
    
    // 添加世界实例引用
    private World world;
    
    // 物理更新任务
    private PhysicsUpdateTask physicsUpdateTask;
    private ScheduledExecutorService scheduler;

    public MovementSync() {
        Instance = this;
        this.world = World.Instance;
        this.movementController = new MovementController();
        this.physicsUpdateTask = new PhysicsUpdateTask(this);
    }
    
    // 获取世界实例
    public World getWorld() {
        return this.world;
    }
    
    // 获取移动控制器
    public MovementController getMovementController() {
        return this.movementController;
    }
    
    // 获取网络会话
    public Session getSession() {
        return Bot.Instance.getSession();
    }

    @Override
    public void onLoad() {
        getLogger().info("Loading MovementSync");
    }

    @Override
    public void onUnload() {
        getLogger().info("Unloading MovementSync");
        if (scheduler != null) {
            scheduler.shutdown();
        }
    }

    @Override
    public void onEnable() {
        getLogger().info("Enabling MovementSync");
        
        // 添加数据包监听器
        Bot.Instance.addPacketListener(new TeleportPacketListener(), this);
        Bot.Instance.addPacketListener(new EntityIdRecorder(), this);
        Bot.Instance.addPacketListener(new RespawnPacketListener(), this);
        Bot.Instance.addPacketListener(new ChunkDataListener(), this);
        Bot.Instance.addPacketListener(new EntityVelocityListener(), this);

        // 注册命令
        registerCommands();
        
        // 启动物理更新任务
        startPhysicsUpdateTask();
    }
    
    private void registerCommands() {
        try {
            // 逐一注册命令，确保每个命令都能正确注册
            registerWhereAmICommand();
            registerMoveCommand();
            registerGravityCommand();
            registerJumpCommand();
            
            getLogger().info("All commands registered successfully");
        } catch (Exception e) {
            getLogger().error("Error registering commands: {}", e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void registerWhereAmICommand() {
        try {
            WhereAmICommand command = new WhereAmICommand();
            WhereAmICommandExecutor executor = new WhereAmICommandExecutor();
            Bot.Instance.getPluginManager().registerCommand(command, executor, this);
            getLogger().info("WhereAmI command registered");
        } catch (Exception e) {
            getLogger().error("Error registering WhereAmI command: {}", e.getMessage());
        }
    }
    
    private void registerMoveCommand() {
        try {
            MoveCommand command = new MoveCommand();
            MoveCommandExecutor executor = new MoveCommandExecutor();
            Bot.Instance.getPluginManager().registerCommand(command, executor, this);
            getLogger().info("Move command registered");
        } catch (Exception e) {
            getLogger().error("Error registering Move command: {}", e.getMessage());
        }
    }
    
    private void registerGravityCommand() {
        try {
            GravityCommand command = new GravityCommand();
            GravityCommandExecutor executor = new GravityCommandExecutor();
            Bot.Instance.getPluginManager().registerCommand(command, executor, this);
            getLogger().info("Gravity command registered");
        } catch (Exception e) {
            getLogger().error("Error registering Gravity command: {}", e.getMessage());
        }
    }
    
    private void registerJumpCommand() {
        try {
            JumpCommand command = new JumpCommand();
            JumpCommandExecutor executor = new JumpCommandExecutor();
            Bot.Instance.getPluginManager().registerCommand(command, executor, this);
            getLogger().info("Jump command registered");
        } catch (Exception e) {
            getLogger().error("Error registering Jump command: {}", e.getMessage());
        }
    }
    
    private void startPhysicsUpdateTask() {
        try {
            scheduler = Executors.newScheduledThreadPool(1);
            physicsUpdateTask.start();
            // 每50毫秒更新一次物理状态（20 TPS）
            scheduler.scheduleAtFixedRate(physicsUpdateTask, 0, 50, TimeUnit.MILLISECONDS);
            getLogger().info("Physics update task started");
        } catch (Exception e) {
            getLogger().error("Error starting physics update task: {}", e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void onDisable() {
        getLogger().info("Disabling MovementSync");
        if (scheduler != null) {
            scheduler.shutdown();
        }
    }
}