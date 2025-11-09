package xin.bbtt.events;

import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.joml.Vector3d;
import xin.bbtt.mcbot.event.Event;
import xin.bbtt.mcbot.event.HandlerList;
import xin.bbtt.mcbot.event.HasDefaultAction;

public class TeleportEvent extends Event implements HasDefaultAction {
    private static final HandlerList HANDLERS = new HandlerList();
    private boolean cancelDefault;
    @Getter
    private final int teleportId;
    @Getter
    private final Vector3d position;

    public TeleportEvent(int teleportId, Vector3d position) {
        this.teleportId = teleportId;
        this.position = position;
    }

    @Override public boolean isDefaultActionCancelled() { return cancelDefault; }
    @Override public void setDefaultActionCancelled(boolean c) { this.cancelDefault = c; }

    @Override public HandlerList getHandlers() { return HANDLERS; }
    public static HandlerList getHandlerList() { return HANDLERS; }
}
