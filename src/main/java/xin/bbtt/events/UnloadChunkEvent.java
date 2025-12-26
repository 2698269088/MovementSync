package xin.bbtt.events;

import lombok.Getter;
import xin.bbtt.mcbot.event.Event;
import xin.bbtt.mcbot.event.HandlerList;

public class UnloadChunkEvent extends Event {
    private final static HandlerList HANDLERS = new HandlerList();

    @Getter
    private final int chunkX;
    @Getter
    private final int chunkZ;

    public UnloadChunkEvent(int chunkX, int chunkZ) {
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }
    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

}
