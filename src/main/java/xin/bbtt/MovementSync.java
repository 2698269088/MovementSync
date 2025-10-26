package xin.bbtt;

import org.joml.Vector3d;
import xin.bbtt.commands.WhereAmICommand;
import xin.bbtt.commands.WhereAmICommandExecutor;
import xin.bbtt.listeners.ChunkDataListener;
import xin.bbtt.listeners.EntityIdRecorder;
import xin.bbtt.listeners.RespawnPacketListener;
import xin.bbtt.listeners.TeleportPacketListener;
import xin.bbtt.mcbot.Bot;
import xin.bbtt.mcbot.plugin.Plugin;

public class MovementSync implements Plugin {
    public static MovementSync Instance;
    public static int entityId = -1;
    public static Vector3d position;

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
    }

    @Override
    public void onDisable() {
        getLogger().info("Disabling MovementSync");
    }
}
