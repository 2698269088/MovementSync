package xin.bbtt;

import org.geysermc.mcprotocollib.protocol.packet.ingame.serverbound.player.ServerboundMovePlayerPosPacket;
import org.joml.Vector3d;
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

public class MovementSync implements Plugin {
    public static MovementSync Instance;
    public int entityId = -1;
    public Vector3d position = new Vector3d();
    public Vector3d velocity = new Vector3d();
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
        Bot.Instance.addPacketListener(new TeleportPacketListener(), this);
        Bot.Instance.addPacketListener(new EntityIdRecorder(), this);
        Bot.Instance.addPacketListener(new RespawnPacketListener(), this);
        Bot.Instance.addPacketListener(new ChunkDataListener(), this);

        Bot.Instance.getPluginManager().registerCommand(new WhereAmICommand(), new WhereAmICommandExecutor(),  this);

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
            Vector3d lastPos = new Vector3d(position);
            this.checkOnGround();
            this.updateMotionState();
            if (!lastPos.equals(position) && Bot.Instance.getServer() == Server.Xin) {
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
        if (position.y -  Math.floor(position.y) > 0.5d) {
            onGround = false;
            return;
        }
        if (World.Instance.getBlockAt(position) != 0) {
            position.y = Math.floor(position.y) + 0.5d;
            onGround = true;
            return;
        }
        onGround = false;
    }

    public void updateMotionState() {
        if (velocity.y > terminalVelocity) {
            velocity.add(gravitationalAcceleration);
        }
        if (onGround && velocity.y < 0) {
            velocity.y = 0;
        }
        position.add(velocity);
    }

    public void syncPositionToServer() {
        Bot.Instance.getSession().send(new ServerboundMovePlayerPosPacket(onGround, position.x, position.y, position.z));
    }

    public void jump() {
        if (onGround) {
            velocity.add(new Vector3d(0, 0.021, 0));
        }
    }
}
