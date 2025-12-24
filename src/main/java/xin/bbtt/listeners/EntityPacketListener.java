package xin.bbtt.listeners;

import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.entity.*;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.entity.spawn.ClientboundAddEntityPacket;
import xin.bbtt.mcbot.event.EventHandler;
import xin.bbtt.mcbot.event.Listener;
import xin.bbtt.mcbot.events.ReceivePacketEvent;
import xin.bbtt.world.World;

public class EntityPacketListener implements Listener {

    @EventHandler
    public void OnAddEntities(ReceivePacketEvent<ClientboundAddEntityPacket> receivePacketEvent) {
        //noinspection DataFlowIssue
        if (!(receivePacketEvent.getPacket() instanceof ClientboundAddEntityPacket)) return;
        World.Instance.handleAddEntityPacket(receivePacketEvent.getPacket());
    }

    @EventHandler
    public void OnMoveEntityPosition(ReceivePacketEvent<ClientboundMoveEntityPosPacket> receivePacketEvent) {
        //noinspection DataFlowIssue
        if (!(receivePacketEvent.getPacket() instanceof ClientboundMoveEntityPosPacket)) return;
        World.Instance.handleMoveEntityPosPacket(receivePacketEvent.getPacket());
    }

    @EventHandler
    public void OnMoveEntityRotation(ReceivePacketEvent<ClientboundMoveEntityRotPacket> receivePacketEvent) {
        //noinspection DataFlowIssue
        if (!(receivePacketEvent.getPacket() instanceof ClientboundMoveEntityRotPacket)) return;
        World.Instance.handleMoveEntityRotPacket(receivePacketEvent.getPacket());
    }

    @EventHandler
    public void OnMoveEntityPositronRotation(ReceivePacketEvent<ClientboundMoveEntityPosRotPacket> receivePacketEvent) {
        //noinspection DataFlowIssue
        if (!(receivePacketEvent.getPacket() instanceof ClientboundMoveEntityPosRotPacket)) return;
        World.Instance.handleMoveEntityPosRotPacket(receivePacketEvent.getPacket());
    }

    @EventHandler
    public void OnMoveEntityRotateHead(ReceivePacketEvent<ClientboundRotateHeadPacket> receivePacketEvent) {
        //noinspection DataFlowIssue
        if (!(receivePacketEvent.getPacket() instanceof ClientboundRotateHeadPacket)) return;
        World.Instance.handleRotateHeadPacket(receivePacketEvent.getPacket());
    }

    @EventHandler
    public void OnRemoveEntity(ReceivePacketEvent<ClientboundRemoveEntitiesPacket> receivePacketEvent) {
        //noinspection DataFlowIssue
        if (!(receivePacketEvent.getPacket() instanceof ClientboundRemoveEntitiesPacket)) return;
        World.Instance.handleRemoveEntitiesPacket(receivePacketEvent.getPacket());
    }
}
