package xin.bbtt.world;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.geysermc.mcprotocollib.protocol.codec.MinecraftCodecHelper;
import org.geysermc.mcprotocollib.protocol.data.game.chunk.ChunkSection;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.level.ClientboundLevelChunkWithLightPacket;
import xin.bbtt.MovementSync;

import java.util.HashMap;
import java.util.Map;

public class World {
    public final static World Instance = new World();
    private World() {}
    private final Map<Integer, Map<Integer, ChunkSection>> chunks = new HashMap<>();

    private void setChunk(ClientboundLevelChunkWithLightPacket levelChunkWithLightPacket) {
        if (!chunks.containsKey(levelChunkWithLightPacket.getX())) {
            chunks.put(levelChunkWithLightPacket.getX(), new HashMap<>());
        }
        ByteBuf chunkBuf = Unpooled.wrappedBuffer(levelChunkWithLightPacket.getChunkData());
        MinecraftCodecHelper helper = new MinecraftCodecHelper();
        ChunkSection section = helper.readChunkSection(chunkBuf);
        chunks.get(levelChunkWithLightPacket.getX()).put(levelChunkWithLightPacket.getZ(), section);
        MovementSync.Instance.getLogger().info("{}", section.getBlockCount());
    }

    public void handleLevelChunkAndLightUpdate(ClientboundLevelChunkWithLightPacket levelChunkWithLightPacket) {
        this.setChunk(levelChunkWithLightPacket);
    }
}
