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
        World.Instance.handleAddEntityPacket(receivePacketEvent.getPacket());
    }

    @EventHandler
    public void OnMoveEntityPosition(ReceivePacketEvent<ClientboundMoveEntityPosPacket> receivePacketEvent) {
        World.Instance.handleMoveEntityPosPacket(receivePacketEvent.getPacket());
    }

    @EventHandler
    public void OnMoveEntityRotation(ReceivePacketEvent<ClientboundMoveEntityRotPacket> receivePacketEvent) {
        World.Instance.handleMoveEntityRotPacket(receivePacketEvent.getPacket());
    }

    @EventHandler
    public void OnMoveEntityPositronRotation(ReceivePacketEvent<ClientboundMoveEntityPosRotPacket> receivePacketEvent) {
        World.Instance.handleMoveEntityPosRotPacket(receivePacketEvent.getPacket());
    }

    @EventHandler
    public void OnMoveEntityRotateHead(ReceivePacketEvent<ClientboundRotateHeadPacket> receivePacketEvent) {
        World.Instance.handleRotateHeadPacket(receivePacketEvent.getPacket());
    }

    @EventHandler
    public void OnRemoveEntity(ReceivePacketEvent<ClientboundRemoveEntitiesPacket> receivePacketEvent) {
        World.Instance.handleRemoveEntitiesPacket(receivePacketEvent.getPacket());
    }
}
