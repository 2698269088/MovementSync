package xin.bbtt.listeners;

import org.geysermc.mcprotocollib.network.Session;
import org.geysermc.mcprotocollib.network.event.session.SessionAdapter;
import org.geysermc.mcprotocollib.network.packet.Packet;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.entity.ClientboundSetEntityMotionPacket;
import xin.bbtt.MovementSync;

public class EntityVelocityListener extends SessionAdapter {
    @Override
    public void packetReceived(Session session, Packet packet) {
        if (!(packet instanceof ClientboundSetEntityMotionPacket entityMotionPacket)) return;
        
        // 检查是否是自己的实体ID
        if (entityMotionPacket.getEntityId() == MovementSync.entityId) {
            // 获取移动控制器并应用动量
            if (MovementSync.Instance != null && MovementSync.Instance.getMovementController() != null) {
                MovementSync.Instance.getMovementController().setMotion(
                    entityMotionPacket.getMotionX(),
                    entityMotionPacket.getMotionY(),
                    entityMotionPacket.getMotionZ()
                );
            }
        }
    }
}