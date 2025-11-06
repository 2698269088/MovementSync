package xin.bbtt.world;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.geysermc.mcprotocollib.protocol.codec.MinecraftCodecHelper;
import org.geysermc.mcprotocollib.protocol.data.game.chunk.ChunkSection;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.level.ClientboundLevelChunkWithLightPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.level.ClientboundSectionBlocksUpdatePacket;
import org.joml.Vector3d;
import xin.bbtt.MovementSync;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class World {
    public final static World Instance = new World();
    private World() {}
    private final Map<Integer, Map<Integer, ChunkSection>> chunks = new ConcurrentHashMap<>();

    public void handleLevelChunkAndLightUpdate(ClientboundLevelChunkWithLightPacket levelChunkWithLightPacket) {
        if (!chunks.containsKey(levelChunkWithLightPacket.getX())) {
            chunks.put(levelChunkWithLightPacket.getX(), new HashMap<>());
        }
        ByteBuf chunkBuf = Unpooled.wrappedBuffer(levelChunkWithLightPacket.getChunkData());
        MinecraftCodecHelper helper = new MinecraftCodecHelper();
        ChunkSection section = helper.readChunkSection(chunkBuf);
        chunks.get(levelChunkWithLightPacket.getX()).put(levelChunkWithLightPacket.getZ(), section);
        MovementSync.Instance.getLogger().info("({}, {})", levelChunkWithLightPacket.getX(), levelChunkWithLightPacket.getZ());
    }

    public void handleSectionBlocksUpdatePacket(ClientboundSectionBlocksUpdatePacket sectionBlocksUpdatePacket) {
        if (!chunks.containsKey(sectionBlocksUpdatePacket.getChunkX())) return;
        Map<Integer, ChunkSection> xChunks = chunks.get(sectionBlocksUpdatePacket.getChunkX());
        if (!xChunks.containsKey(sectionBlocksUpdatePacket.getChunkZ())) return;
        ChunkSection section = xChunks.get(sectionBlocksUpdatePacket.getChunkZ());
        Arrays.stream(sectionBlocksUpdatePacket.getEntries()).forEach(entry -> {
             int relativeX = entry.getPosition().getX() & 15;
             int relativeZ = entry.getPosition().getZ() & 15;
            MovementSync.Instance.getLogger().info("({}, {}, {}), {}", relativeX, entry.getPosition().getY(), relativeZ, entry.getBlock());
        });
    }

    public int getBlockAt(Vector3d position) {
        int chunkX = (int)Math.floor(position.x / 16);
        int chunkZ = (int)Math.floor(position.z / 16);
        // MovementSync.Instance.getLogger().info("({}, {})", chunkX, chunkZ);
        if (!chunks.containsKey(chunkX)) return -1;
        Map<Integer, ChunkSection> xChunks = chunks.get(chunkX);
        if (!xChunks.containsKey(chunkZ)) return-1;
        ChunkSection section = xChunks.get(chunkZ);
        try {
            return section.getBlock((int)position.x & 15, (int)position.y, (int)position.z & 15);
        }
        catch (IndexOutOfBoundsException e) {
            return -2;
        }
    }
}
