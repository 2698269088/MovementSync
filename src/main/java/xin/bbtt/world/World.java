package xin.bbtt.world;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.geysermc.mcprotocollib.protocol.codec.MinecraftCodecHelper;
import org.geysermc.mcprotocollib.protocol.data.game.chunk.ChunkSection;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.level.ClientboundLevelChunkWithLightPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.level.ClientboundSectionBlocksUpdatePacket;
import org.joml.Vector3d;
import xin.bbtt.MovementSync;
import xin.bbtt.mcbot.Bot;
import xin.bbtt.mcbot.Server;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class World {
    public final static World Instance = new World();
    private World() {}
    private final Map<Integer, Map<Integer, List<ChunkSection>>> chunks = new ConcurrentHashMap<>();

    public void handleLevelChunkAndLightUpdate(ClientboundLevelChunkWithLightPacket levelChunkWithLightPacket) {
        if (!chunks.containsKey(levelChunkWithLightPacket.getX())) {
            chunks.put(levelChunkWithLightPacket.getX(), new HashMap<>());
        }
        ByteBuf chunkBuf = Unpooled.wrappedBuffer(levelChunkWithLightPacket.getChunkData());
        MinecraftCodecHelper helper = new MinecraftCodecHelper();
        List<ChunkSection> sections = new ArrayList<>();
        ChunkSection section;
        while (chunkBuf.isReadable() && (section = helper.readChunkSection(chunkBuf)) != null) {
            sections.add(section);
        }
        chunks.get(levelChunkWithLightPacket.getX()).put(levelChunkWithLightPacket.getZ(), sections);
    }

    public void handleSectionBlocksUpdatePacket(ClientboundSectionBlocksUpdatePacket sectionBlocksUpdatePacket) {
        if (!chunks.containsKey(sectionBlocksUpdatePacket.getChunkX())) return;
        Map<Integer, List<ChunkSection>> xChunks = chunks.get(sectionBlocksUpdatePacket.getChunkX());
        if (!xChunks.containsKey(sectionBlocksUpdatePacket.getChunkZ())) return;
        List<ChunkSection> sections = xChunks.get(sectionBlocksUpdatePacket.getChunkZ());
        ChunkSection section = sections.get(sectionBlocksUpdatePacket.getChunkY());
        Arrays.stream(sectionBlocksUpdatePacket.getEntries()).forEach(entry -> {
            int relativeX = entry.getPosition().getX() & 15;
            int relativeZ = entry.getPosition().getZ() & 15;
            int relativeY = entry.getPosition().getY() & 15;
            section.setBlock(relativeX, relativeY, relativeZ, entry.getBlock());
        });
    }

    public int getBlockAt(Vector3d position) {
        int chunkX = (int)Math.floor(position.x / 16);
        int chunkZ = (int)Math.floor(position.z / 16);
        int chunkY;
        if (Bot.Instance.getServer() == Server.Xin) {
            chunkY = ((int)Math.floor(position.y + 64) / 16);
        }
        else {
            chunkY = (int)Math.floor(position.y / 16);
        }
        if (!chunks.containsKey(chunkX)) return -1;
        Map<Integer, List<ChunkSection>> xChunks = chunks.get(chunkX);
        if (!xChunks.containsKey(chunkZ)) return-1;
        List<ChunkSection> zChunks = xChunks.get(chunkZ);
        if (chunkY >= zChunks.size()) return -1;
        ChunkSection section = zChunks.get(chunkY);
        try {
            return section.getBlock((int)position.x & 15, (int)position.y & 15, (int)position.z & 15);
        }
        catch (IndexOutOfBoundsException e) {
            return -2;
        }
    }
}
