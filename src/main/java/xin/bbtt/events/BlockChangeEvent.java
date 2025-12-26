package xin.bbtt.events;

import lombok.Getter;
import lombok.Setter;
import org.joml.Vector3i;
import xin.bbtt.mcbot.event.Event;
import xin.bbtt.mcbot.event.HandlerList;

public class BlockChangeEvent extends Event {
    private final static HandlerList HANDLERS = new HandlerList();

    @Getter
    final private Vector3i position;
    @Getter
    final private int previous;
    @Getter
    @Setter
    private int changeTo;

    public BlockChangeEvent(Vector3i position, int previous, int changeTo) {
        this.position = position;
        this.previous = previous;
        this.changeTo = changeTo;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }
    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
