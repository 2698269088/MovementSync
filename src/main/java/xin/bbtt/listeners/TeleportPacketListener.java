package xin.bbtt.listeners;

import org.geysermc.mcprotocollib.network.Session;
import org.geysermc.mcprotocollib.network.event.session.SessionAdapter;
import org.geysermc.mcprotocollib.network.packet.Packet;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.entity.player.ClientboundPlayerPositionPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.serverbound.level.ServerboundAcceptTeleportationPacket;
import org.joml.Vector3d;
import xin.bbtt.MovementSync;
import xin.bbtt.events.TeleportEvent;
import xin.bbtt.mcbot.Bot;

import xin.bbtt.tasks.VanillaPhysicsTask;

public class TeleportPacketListener extends SessionAdapter {
    @Override
    public synchronized void packetReceived(Session session, Packet packet) {
        // 添加空值检查
        if (MovementSync.Instance == null || Bot.Instance == null) return;
        
        if (!(packet instanceof ClientboundPlayerPositionPacket playerPositionPacket)) return;
        
        Vector3d position = new Vector3d(playerPositionPacket.getX(), playerPositionPacket.getY(), playerPositionPacket.getZ());
        TeleportEvent teleportEvent = new TeleportEvent(playerPositionPacket.getTeleportId(), position);
        
        try {
            Bot.Instance.getPluginManager().events().callEvent(teleportEvent);
            if (teleportEvent.isDefaultActionCancelled()) return;
            
            // 安全的位置更新
            if (MovementSync.Instance.position != null) {
                MovementSync.Instance.position.set(position);
            }
            if (MovementSync.Instance.pitch != null) {
                MovementSync.Instance.pitch.set(playerPositionPacket.getPitch());
            }
            if (MovementSync.Instance.yaw != null) {
                MovementSync.Instance.yaw.set(playerPositionPacket.getYaw());
            }
            
            session.send(new ServerboundAcceptTeleportationPacket(playerPositionPacket.getTeleportId()));
            
            // 安全地取消移动和重置速度
            if (MovementSync.Instance.movementController != null) {
                MovementSync.Instance.movementController.cancelAll();
            }
            if (MovementSync.Instance.velocity != null) {
                MovementSync.Instance.velocity.set(new Vector3d());
            }
            
            // 使用新的地面检查方法
            VanillaPhysicsTask.jump(); // 这里借用jump方法中的地面检查逻辑
            
        } catch (Exception e) {
            MovementSync.Instance.getLogger().error("Error handling teleport packet", e);
        }
    }
}
