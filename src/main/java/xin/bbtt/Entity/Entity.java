package xin.bbtt.Entity;

import lombok.Data;
import lombok.NonNull;
import org.geysermc.mcprotocollib.protocol.data.game.entity.type.EntityType;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.entity.spawn.ClientboundAddEntityPacket;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3d;

import java.util.UUID;

@Data
public class Entity {
    private int entityId;
    private final @NonNull UUID uuid;
    private final @NonNull EntityType type;
    private Vector3d position;
    private float yaw;
    private float headYaw;
    private float pitch;
    private double motionX;
    private double motionY;
    private double motionZ;

    public Entity(int entityId, @NotNull UUID uuid, @NotNull EntityType type, double x, double y, double z, float yaw, float headYaw, float pitch, double motionX, double motionY, double motionZ) {
        this.entityId = entityId;
        this.uuid = uuid;
        this.type = type;
        this.position = new Vector3d(x, y, z);
        this.yaw = yaw;
        this.headYaw = headYaw;
        this.pitch = pitch;
        this.motionX = motionX;
        this.motionY = motionY;
        this.motionZ = motionZ;
    }

    public static Entity fromPacket(ClientboundAddEntityPacket packet) {
        return new Entity(packet.getEntityId(), packet.getUuid(), packet.getType(), packet.getX(), packet.getY(), packet.getZ(), packet.getYaw(), packet.getHeadYaw(), packet.getPitch(), packet.getMotionX(), packet.getMotionY(), packet.getMotionZ());
    }

    public void move(Vector3d delta){
        position.add(delta);
    }

    public void moveTo(Vector3d position) {
        this.position = position;
    }
}
