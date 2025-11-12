package xin.bbtt.world;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.geysermc.mcprotocollib.protocol.codec.MinecraftCodecHelper;
import org.geysermc.mcprotocollib.protocol.data.game.chunk.ChunkSection;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.level.ClientboundLevelChunkWithLightPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.level.ClientboundSectionBlocksUpdatePacket;
import org.joml.Vector3d;
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
        ChunkSection section;
        if (Bot.Instance.getServer() == Server.Xin) {
            section = sections.get(sectionBlocksUpdatePacket.getChunkY() + 4);
        }
        else {
            section = sections.get(sectionBlocksUpdatePacket.getChunkY());
        }
        Arrays.stream(sectionBlocksUpdatePacket.getEntries()).forEach(entry -> {
            int relativeX = entry.getPosition().getX() & 15;
            int relativeZ = entry.getPosition().getZ() & 15;
            int relativeY = entry.getPosition().getY() & 15;
            section.setBlock(relativeX, relativeY, relativeZ, entry.getBlock());
        });
    }

    public Block getBlockAt(Vector3d position) {
        int chunkX = (int) Math.floor(position.x / 16);
        int chunkZ = (int) Math.floor(position.z / 16);
        int chunkY;

        if (Bot.Instance.getServer() == Server.Xin) {
            chunkY = ((int) Math.floor(position.y + 64) / 16);
        } else {
            chunkY = (int) Math.floor(position.y / 16);
        }

        if (!chunks.containsKey(chunkX)) return Block.AIR;
        Map<Integer, List<ChunkSection>> xChunks = chunks.get(chunkX);
        if (!xChunks.containsKey(chunkZ)) return Block.AIR;
        List<ChunkSection> zChunks = xChunks.get(chunkZ);
        if (chunkY >= zChunks.size() || chunkY < 0) return Block.AIR;

        ChunkSection section = zChunks.get(chunkY);
        try {
            int blockId = section.getBlock(
                    (int) Math.floor(position.x) & 15,
                    (int) Math.floor(position.y) & 15,
                    (int) Math.floor(position.z) & 15
            );

            // 使用Block缓存池优化性能
            return new Block(blockId);
        } catch (IndexOutOfBoundsException e) {
            return Block.AIR;
        }
    }

    /**
     * 获取方块材料
     */
    public Material getBlockMaterial(Vector3d position) {
        return getBlockAt(position).getType();
    }

    /**
     * 检查方块是否可站立
     */
    public boolean canStandOn(Vector3d position) {
        return getBlockMaterial(position).isSolid();
    }
}
