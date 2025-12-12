package xin.bbtt;

import org.joml.Vector3d;
import xin.bbtt.commands.JumpCommand;
import xin.bbtt.commands.JumpCommandExecutor;
import xin.bbtt.commands.WhereAmICommand;
import xin.bbtt.commands.WhereAmICommandExecutor;
import xin.bbtt.listeners.*;
import xin.bbtt.mcbot.Bot;
import xin.bbtt.mcbot.plugin.Plugin;
import xin.bbtt.tasks.updateMotionTask;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class MovementSync implements Plugin {
    public static MovementSync Instance;
    public int entityId = -1;
    public AtomicReference<Vector3d> position = new AtomicReference<>();
    public AtomicReference<Vector3d> velocity = new AtomicReference<>();
    public static final Vector3d gravitationalAcceleration = new Vector3d(0, -0.08, 0);
    public static final double terminalVelocity = -3.92;
    public AtomicBoolean onGround = new AtomicBoolean(true);
    private ScheduledExecutorService physicalSimulationService;

    public MovementSync() {
        Instance = this;
    }

    @Override
    public void onLoad() {
        getLogger().info("Loading MovementSync");
    }

    @Override
    public void onUnload() {
        getLogger().info("Unloading MovementSync");
    }

    @Override
    public void onEnable() {
        getLogger().info("Enabling MovementSync");
        position.set(new Vector3d(0, 0, 0));
        velocity.set(new Vector3d(0, 0, 0));

        Bot.Instance.addPacketListener(new TeleportPacketListener(), this);
        Bot.Instance.addPacketListener(new EntityIdRecorder(), this);
        Bot.Instance.addPacketListener(new RespawnPacketListener(), this);
        Bot.Instance.addPacketListener(new ChunkDataListener(), this);

        Bot.Instance.getPluginManager().registerCommand(new WhereAmICommand(), new WhereAmICommandExecutor(),  this);
        Bot.Instance.getPluginManager().registerCommand(new JumpCommand(), new JumpCommandExecutor(),  this);

        Bot.Instance.getPluginManager().events().registerEvents(new ServerChangeListener(),  this);

        physicalSimulationService = Executors.newScheduledThreadPool(1);
        physicalSimulationService.scheduleAtFixedRate(new updateMotionTask(), 0, 50, TimeUnit.MILLISECONDS);
    }

    @Override
    public void onDisable() {
        getLogger().info("Disabling MovementSync");
        physicalSimulationService.shutdown();
    }

    public void jump() {
        if (onGround.get()) {
            MovementSync.Instance.getLogger().info("jumping");
            onGround.set(false);
            velocity.updateAndGet(p -> new Vector3d(p).add(new Vector3d(0, 0.42, 0)));
        }
    }
}
