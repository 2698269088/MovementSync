package xin.bbtt.world;

import lombok.Getter;
import org.geysermc.mcprotocollib.protocol.data.game.level.block.BlockEntityInfo;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.level.ClientboundLevelChunkWithLightPacket;
import xin.bbtt.MovementSync;

import java.util.Arrays;

public class Chunk {
    public Chunk(ClientboundLevelChunkWithLightPacket levelChunkWithLightPacket) {
        MovementSync.Instance.getLogger().info(levelChunkWithLightPacket.toString());
    }
}
