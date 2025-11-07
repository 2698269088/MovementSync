package xin.bbtt;

import org.geysermc.mcprotocollib.protocol.data.game.entity.player.PlayerAction;
import org.geysermc.mcprotocollib.protocol.packet.ingame.serverbound.player.ServerboundMovePlayerPosPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.serverbound.player.ServerboundPlayerActionPacket;
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
        getLogger().info("Checking onGround: ({}, {}, {}), {}", position.x, position.y, position.z, World.Instance.getBlockAt(position));
        if (Math.abs(position.y - Math.round(position.y)) < 0.03 && World.Instance.getBlockAt(position) != 0) {
            onGround.set(true);
            return;
        }
        if (World.Instance.getBlockAt(position) != 0) {
            onGround.set(true);
            position.y = Math.round(position.y);
            this.position.set(position);
            return;
        }
        onGround.set(false);
    }

    public void updateMotionState() {
        Vector3d velocity = this.velocity.get();
        if (velocity.y > terminalVelocity) {
            velocity.add(gravitationalAcceleration);
        } else if (velocity.y < 0) {
            velocity.y = terminalVelocity;
        }
        if (onGround.get() && velocity.y < 0) {
            velocity.y = 0;
        }
        velocity.y *= 0.98;
        Vector3d position = new Vector3d(this.position.get());
        Vector3d displacement = new Vector3d(this.velocity.get());
        displacement.add(gravitationalAcceleration.div(2));
        position.add(displacement);
        this.velocity.set(velocity);
        this.position.set(position);
    }

    public void syncPositionToServer() {
        Bot.Instance.getSession().send(new ServerboundMovePlayerPosPacket(onGround.get(), position.get().x, position.get().y, position.get().z));
        getLogger().info("Synced position to server: ({}, {}, {}, {}), vertical velocity: {}b/t", onGround, position.get().x, position.get().y, position.get().z, velocity.get().y);
    }

    public void jump() {
        if (onGround.get()) {
            MovementSync.Instance.getLogger().info("jumping");
            velocity.updateAndGet(p -> new Vector3d(p).add(new Vector3d(0, 0.42, 0)));
        }
    }
}
