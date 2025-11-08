package xin.bbtt;

import org.geysermc.mcprotocollib.protocol.packet.ingame.serverbound.player.ServerboundMovePlayerPosPacket;
import org.joml.Vector3d;
import xin.bbtt.commands.JumpCommand;
import xin.bbtt.commands.JumpCommandExecutor;
import xin.bbtt.commands.WhereAmICommand;
import xin.bbtt.commands.WhereAmICommandExecutor;
import xin.bbtt.listeners.*;
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

        Bot.Instance.getPluginManager().events().registerEvents(new ServerChangeListener(),  this);

        physicalSimulation = new Thread(this::physicalSimulation);
        physicalSimulation.start();
    }

    @Override
    public void onDisable() {
        getLogger().info("Disabling MovementSync");
        physicalSimulation.interrupt();
    }

    public void physicalSimulation() {
        final long interval = 50_000_000L;
        long nextTick = System.nanoTime();

        while (Bot.Instance.isRunning() && !Thread.currentThread().isInterrupted()) {
            if (Bot.Instance.getServer() != Server.Xin) {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    break;
                }
                continue;
            }
            Vector3d lastPos = new Vector3d(position.get());
            updateMotionState();
            checkOnGround();
            if (!lastPos.equals(position.get()) && Bot.Instance.getServer() == Server.Xin) {
                syncPositionToServer();
            }

            nextTick += interval;
            long sleepTime = (nextTick - System.nanoTime()) / 1_000_000L;

            if (sleepTime > 0) {
                try {
                    Thread.sleep(sleepTime);
                } catch (InterruptedException e) {
                    break;
                }
            } else {
                nextTick = System.nanoTime();
            }
        }
    }


    public void checkOnGround() {
        Vector3d position = new Vector3d(this.position.get());
        Vector3d bottomBlockPos = new Vector3d(Math.floor(position.x), Math.round(position.y - 1), Math.floor(position.z));
        if (Math.abs(position.y - Math.round(position.y)) < 0.1 && World.Instance.getBlockAt(bottomBlockPos) != 0) {
            onGround.set(true);
            return;
        }
        onGround.set(false);
    }

    public void updateMotionState() {
        Vector3d velocity = this.velocity.get();
        Vector3d displacement = new Vector3d();
        if (velocity.y > terminalVelocity) {
            velocity.add(gravitationalAcceleration);
            velocity.y *= 0.98;
            displacement.add(this.velocity.get());
            displacement.add(new Vector3d(gravitationalAcceleration).div(2).mul(0.98));
        } else if (velocity.y < 0) {
            velocity.y = terminalVelocity;
            displacement.add(this.velocity.get().add(velocity).div(2));
        }

        if(onGround.get()) {
            velocity.y = 0;
            displacement.y = 0;
        }
        Vector3d position = new Vector3d(this.position.get());
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
            onGround.set(false);
            velocity.updateAndGet(p -> new Vector3d(p).add(new Vector3d(0, 0.42, 0)));
        }
    }
}
