package xin.bbtt.listeners;

import org.geysermc.mcprotocollib.network.Session;
import org.geysermc.mcprotocollib.network.event.session.SessionAdapter;
import org.geysermc.mcprotocollib.network.packet.Packet;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.level.ClientboundBlockUpdatePacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.level.ClientboundForgetLevelChunkPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.level.ClientboundLevelChunkWithLightPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.level.ClientboundSectionBlocksUpdatePacket;
import xin.bbtt.world.World;

public class ChunkDataListener extends SessionAdapter {
    @Override
    public void packetReceived(Session session, Packet packet) {
        if (packet instanceof ClientboundLevelChunkWithLightPacket levelChunkWithLightPacket) World.Instance.handleLevelChunkAndLightUpdate(levelChunkWithLightPacket);
        if (packet instanceof ClientboundSectionBlocksUpdatePacket sectionBlocksUpdatePacket) World.Instance.handleSectionBlocksUpdatePacket(sectionBlocksUpdatePacket);
        if (packet instanceof ClientboundBlockUpdatePacket blockUpdatePacket) World.Instance.handleBlockUpdatePacket(blockUpdatePacket);
        if (packet instanceof ClientboundForgetLevelChunkPacket forgetLevelChunkPacket) World.Instance.handleForgetLevelChunkPacket(forgetLevelChunkPacket);
    }
}
