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

public class TeleportPacketListener extends SessionAdapter {
    // 添加一个变量来跟踪上次记录的位置
    private Vector3d lastLoggedPosition = null;
    // 设置最小变化阈值
    private static final double MIN_CHANGE_THRESHOLD = 0.01;
    // 增加高度变化阈值，减少因微小抖动导致的日志
    private static final double MIN_VERTICAL_CHANGE_THRESHOLD = 0.05;

    @Override
    public void packetReceived(Session session, Packet packet) {
        if (!(packet instanceof ClientboundPlayerPositionPacket playerPositionPacket)) return;
        
        Vector3d position = new Vector3d(playerPositionPacket.getX(), playerPositionPacket.getY(), playerPositionPacket.getZ());
        
        TeleportEvent teleportEvent = new TeleportEvent(playerPositionPacket.getTeleportId(), position);
        Bot.Instance.getPluginManager().events().callEvent(teleportEvent);
        if (teleportEvent.isDefaultActionCancelled()) return;
        MovementSync.position = position;
        
        // 更新移动控制器中的位置
        if (MovementSync.Instance.getMovementController() != null) {
            MovementSync.Instance.getMovementController().setPosition(position);
        }
        
        session.send(new ServerboundAcceptTeleportationPacket(playerPositionPacket.getTeleportId()));
    }
}