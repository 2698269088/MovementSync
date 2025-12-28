package xin.bbtt.listeners;

import org.joml.Vector3d;
import xin.bbtt.MovementSync;
import xin.bbtt.mcbot.event.EventHandler;
import xin.bbtt.mcbot.event.Listener;
import xin.bbtt.mcbot.events.ServerChangeEvent;

public class ServerChangeListener implements Listener {
    @EventHandler
    public void onServerChangeEvent(ServerChangeEvent event)
    {
        MovementSync.Instance.onGround.set(true);
        MovementSync.Instance.velocity.set(new Vector3d());
        MovementSync.Instance.getWorld().clear();
    }
}
