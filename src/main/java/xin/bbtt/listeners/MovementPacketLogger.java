package xin.bbtt.listeners;

import org.geysermc.mcprotocollib.network.Session;
import org.geysermc.mcprotocollib.network.event.session.SessionAdapter;
import org.geysermc.mcprotocollib.network.packet.Packet;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.entity.player.ClientboundPlayerPositionPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.serverbound.player.ServerboundMovePlayerPosRotPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.serverbound.player.ServerboundMovePlayerRotPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.serverbound.player.ServerboundMovePlayerPosPacket;
import org.joml.Vector3d;
import xin.bbtt.MovementSync;
import xin.bbtt.mcbot.Bot;
import xin.bbtt.mcbot.Server;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 移动包调试监听器
 * 用于验证移动包是否成功发送和接收
 */
public class MovementPacketLogger extends SessionAdapter {
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss.SSS");
    private int sentPacketCount = 0;
    private int receivedPacketCount = 0;
    
    @Override
    public synchronized void packetSent(Session session, Packet packet) {
        if (MovementSync.Instance == null || Bot.Instance == null) return;
        
        // 记录客户端发送的移动包（发往服务器）
        if (packet instanceof ServerboundMovePlayerPosRotPacket movePacket) {
            sentPacketCount++;
            logMovementPacket("发送移动包 (位置 + 旋转)", movePacket);
        } else if (packet instanceof ServerboundMovePlayerPosPacket movePacket) {
            sentPacketCount++;
            logMovementPacket("发送移动包 (仅位置)", movePacket);
        } else if (packet instanceof ServerboundMovePlayerRotPacket movePacket) {
            sentPacketCount++;
            logMovementPacket("发送移动包 (仅旋转)", movePacket);
        }
        
        // 记录所有发送的包（调试用）
        MovementSync.Instance.getLogger().debug("已发送包：" + packet.getClass().getSimpleName());
    }
    
    @Override
    public synchronized void packetReceived(Session session, Packet packet) {
        if (MovementSync.Instance == null || Bot.Instance == null) return;
        
        // 记录服务器发送的位置包（客户端接收）
        if (packet instanceof ClientboundPlayerPositionPacket positionPacket) {
            receivedPacketCount++;
            logTeleportPacket("接收瞬移包", positionPacket);
        }
        
        // 记录所有接收的包（调试用）
        MovementSync.Instance.getLogger().debug("已接收包：" + packet.getClass().getSimpleName());
    }
    
    /**
     * 记录移动包详细信息
     */
    private void logMovementPacket(String type, ServerboundMovePlayerPosRotPacket packet) {
        String timestamp = dateFormat.format(new Date());
        Server currentServer = Bot.Instance.getServer();
        
        MovementSync.Instance.getLogger().info(String.format(
            "[%s] %s #%d | 当前服务器：%s | 类型：ServerboundMovePlayerPosRotPacket",
            timestamp, type, sentPacketCount,
            currentServer != null ? currentServer.name() : "null"
        ));
    }
    
    /**
     * 记录仅位置的移动包
     */
    private void logMovementPacket(String type, ServerboundMovePlayerPosPacket packet) {
        String timestamp = dateFormat.format(new Date());
        Server currentServer = Bot.Instance.getServer();
        
        MovementSync.Instance.getLogger().info(String.format(
            "[%s] %s #%d | 当前服务器：%s | 类型：ServerboundMovePlayerPosPacket",
            timestamp, type, sentPacketCount,
            currentServer != null ? currentServer.name() : "null"
        ));
    }
    
    /**
     * 记录仅旋转的移动包
     */
    private void logMovementPacket(String type, ServerboundMovePlayerRotPacket packet) {
        String timestamp = dateFormat.format(new Date());
        Server currentServer = Bot.Instance.getServer();
        
        MovementSync.Instance.getLogger().info(String.format(
            "[%s] %s #%d | 当前服务器：%s | 类型：ServerboundMovePlayerRotPacket",
            timestamp, type, sentPacketCount,
            currentServer != null ? currentServer.name() : "null"
        ));
    }
    
    /**
     * 记录瞬移包详细信息
     */
    private void logTeleportPacket(String type, ClientboundPlayerPositionPacket packet) {
        String timestamp = dateFormat.format(new Date());
        Vector3d pos = new Vector3d(packet.getX(), packet.getY(), packet.getZ());
        float yaw = packet.getYaw();
        float pitch = packet.getPitch();
        
        MovementSync.Instance.getLogger().info(String.format(
            "[%s] %s #%d | 瞬移 ID: %d | 位置：%.4f, %.4f, %.4f | 角度：yaw=%.2f, pitch=%.2f",
            timestamp, type, receivedPacketCount,
            packet.getTeleportId(),
            pos.x, pos.y, pos.z,
            yaw, pitch
        ));
    }
    
    /**
     * 获取已发送的包数量
     */
    public int getSentPacketCount() {
        return sentPacketCount;
    }
    
    /**
     * 获取已接收的包数量
     */
    public int getReceivedPacketCount() {
        return receivedPacketCount;
    }
}
