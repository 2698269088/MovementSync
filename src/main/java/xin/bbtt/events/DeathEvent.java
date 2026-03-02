package xin.bbtt.events;

import lombok.Getter;
import net.kyori.adventure.text.Component;
import xin.bbtt.mcbot.event.Event;
import xin.bbtt.mcbot.event.HandlerList;
import xin.bbtt.mcbot.event.HasDefaultAction;

public class DeathEvent extends Event implements HasDefaultAction {
    private static final HandlerList HANDLERS = new HandlerList();
    private boolean cancelDefault;
    @Getter
    private final int playerId;
    @Getter
    private final Component message;

    public DeathEvent(int playerId, Component message) {
        this.playerId = playerId;
        this.message = message;
    }

    @Override public boolean isDefaultActionCancelled() { return cancelDefault; }
    @Override public void setDefaultActionCancelled(boolean c) { this.cancelDefault = c; }

    @Override public HandlerList getHandlers() { return HANDLERS; }
    public static HandlerList getHandlerList() { return HANDLERS; }
}
