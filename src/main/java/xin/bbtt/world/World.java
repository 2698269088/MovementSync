package xin.bbtt.world;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.geysermc.mcprotocollib.protocol.codec.MinecraftCodecHelper;
import org.geysermc.mcprotocollib.protocol.data.game.chunk.ChunkSection;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.level.ClientboundForgetLevelChunkPacket;
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

    public static boolean isOnGround(Vector3d position) {
        Vector3d bottomBlockPos = new Vector3d(position).floor().add(Direction.DOWN.getUnitVector());

        if (position.y > (int)position.y + 0.0001) {
            bottomBlockPos = position;
            bottomBlockPos.y = (int)position.y;
        }

        if (World.Instance.getBlockAt(bottomBlockPos) != 0){
            return true;
        }
        // North
        if (1 + Math.floor(position.z) - position.z > 0.7) {
            return World.Instance.getBlockAt(new Vector3d(bottomBlockPos).add(Direction.NORTH.getUnitVector())) != 0;
        }
        // East
        if (position.x - Math.floor(position.x) > 0.7) {
            return World.Instance.getBlockAt(new Vector3d(bottomBlockPos).add(Direction.EAST.getUnitVector())) != 0;
        }
        // South
        if (position.z - Math.floor(position.z) > 0.7) {
            return World.Instance.getBlockAt(new Vector3d(bottomBlockPos).add(Direction.SOUTH.getUnitVector())) != 0;
        }
        // West
        if (1 + Math.floor(position.x) - position.x > 0.7) {
            return World.Instance.getBlockAt(new Vector3d(bottomBlockPos).add(Direction.WEST.getUnitVector())) != 0;
        }
        return false;
    }

    public void clear(){
        chunks.clear();
    }

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

    public void handleForgetLevelChunkPacket(ClientboundForgetLevelChunkPacket forgetLevelChunkPacket) {
        if (!chunks.containsKey(forgetLevelChunkPacket.getX())) return;
        if (!chunks.get(forgetLevelChunkPacket.getX()).containsKey(forgetLevelChunkPacket.getZ())) return;
        chunks.get(forgetLevelChunkPacket.getX()).remove(forgetLevelChunkPacket.getZ());
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
        if (!xChunks.containsKey(chunkZ)) return -1;
        List<ChunkSection> zChunks = xChunks.get(chunkZ);
        if (chunkY >= zChunks.size() || chunkY < 0) return -1;
        ChunkSection section = zChunks.get(chunkY);
        try {
            return section.getBlock((int)Math.floor(position.x) & 15, (int)Math.floor(position.y) & 15, (int)Math.floor(position.z) & 15);
        }
        catch (IndexOutOfBoundsException e) {
            return -1;
        }
    }
}
