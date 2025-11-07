package xin.bbtt;

import org.geysermc.mcprotocollib.protocol.packet.ingame.serverbound.player.ServerboundMovePlayerPosPacket;
import org.joml.Vector3d;
import xin.bbtt.commands.JumpCommand;
import xin.bbtt.commands.JumpCommandExecutor;
import xin.bbtt.commands.WhereAmICommand;
import xin.bbtt.commands.WhereAmICommandExecutor;
import xin.bbtt.listeners.ChunkDataListener;
import xin.bbtt.listeners.EntityIdRecorder;
import xin.bbtt.listeners.RespawnPacketListener;
import xin.bbtt.listeners.TeleportPacketListener;
import xin.bbtt.mcbot.Bot;
import xin.bbtt.mcbot.Server;
import xin.bbtt.mcbot.plugin.Plugin;
import xin.bbtt.world.World;

import java.util.concurrent.atomic.AtomicReference;

public class MovementSync implements Plugin {
    public static MovementSync Instance;
    public int entityId = -1;
    public AtomicReference<Vector3d> position = new AtomicReference<>();
    public AtomicReference<Vector3d> velocity = new AtomicReference<>();
    public static final Vector3d gravitationalAcceleration = new Vector3d(0, -0.08, 0);
    public static final double terminalVelocity = -3.92;
    public boolean onGround;
    private Thread physicalSimulation;

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

        physicalSimulation = new Thread(this::physicalSimulation);
        physicalSimulation.start();
    }

    @Override
    public void onDisable() {
        getLogger().info("Disabling MovementSync");
        physicalSimulation.interrupt();
    }

    public void physicalSimulation() {
        while (Bot.Instance.isRunning()) {
            Vector3d lastPos = new Vector3d(position.get());
            this.updateMotionState();
            this.checkOnGround();
            if (!lastPos.equals(position.get()) && Bot.Instance.getServer() == Server.Xin) {
                this.syncPositionToServer();
            }
            try {
                Thread.sleep(50L);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    public void checkOnGround() {
        Vector3d position = new Vector3d(this.position.get());
        if (World.Instance.getBlockAt(position) != 0) {
            onGround = true;
            position.y = Math.round(position.y);
            this.position.set(position);
            return;
        }
        onGround = false;
    }

    public void updateMotionState() {
        if (velocity.get().y > terminalVelocity) {
            velocity.updateAndGet(v -> new Vector3d(v).add(gravitationalAcceleration));
        }
        if (onGround && velocity.get().y < 0) {
            Vector3d newVelocity = velocity.get();
            newVelocity.y = 0;
            velocity.set(newVelocity);
        }
        position.updateAndGet(p -> new Vector3d(p).add(velocity.get()));
    }

    public void syncPositionToServer() {
        Bot.Instance.getSession().send(new ServerboundMovePlayerPosPacket(onGround, position.get().x, position.get().y, position.get().z));
        getLogger().info("Sync position to server: ({}, {}, {}, {})", onGround, position.get().x, position.get().y, position.get().z);
    }

    public void jump() {
        if (onGround) {
            velocity.updateAndGet(p -> new Vector3d(p).add(new Vector3d(0, 0.42, 0)));
        }
    }
}
