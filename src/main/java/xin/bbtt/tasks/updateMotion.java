package xin.bbtt.tasks;

import org.geysermc.mcprotocollib.protocol.packet.ingame.serverbound.player.ServerboundMovePlayerPosPacket;
import org.joml.Vector3d;
import xin.bbtt.MovementSync;
import xin.bbtt.mcbot.Bot;
import xin.bbtt.mcbot.Server;
import xin.bbtt.world.World;

import static xin.bbtt.MovementSync.gravitationalAcceleration;
import static xin.bbtt.MovementSync.terminalVelocity;

public class updateMotion implements Runnable {

    public void syncPositionToServer() {
        Bot.Instance.getSession().send(new ServerboundMovePlayerPosPacket(MovementSync.Instance.onGround.get(), MovementSync.Instance.position.get().x, MovementSync.Instance.position.get().y, MovementSync.Instance.position.get().z));
        MovementSync.Instance.getLogger().info("Synced position to server: ({}, {}, {}, {}), vertical velocity: {}b/t", MovementSync.Instance.onGround, MovementSync.Instance.position.get().x, MovementSync.Instance.position.get().y, MovementSync.Instance.position.get().z, MovementSync.Instance.velocity.get().y);
    }

    public void checkOnGround() {
        Vector3d position = new Vector3d(MovementSync.Instance.position.get());
        Vector3d bottomBlockPos = new Vector3d(Math.floor(position.x), Math.round(position.y - 1), Math.floor(position.z));
        if (Math.abs(position.y - Math.round(position.y)) < 0.1 && World.Instance.getBlockAt(bottomBlockPos) != 0) {
            MovementSync.Instance.onGround.set(true);
            return;
        }
        MovementSync.Instance.onGround.set(false);
    }

    @Override
    public void run() {
        if (!Bot.Instance.isRunning()) return;
        if (Bot.Instance.getServer() != Server.Xin) return;
        Vector3d lastPos = new Vector3d(MovementSync.Instance.position.get());
        checkOnGround();

        Vector3d velocity = MovementSync.Instance.velocity.get();
        Vector3d displacement = new Vector3d();
        if (velocity.y > terminalVelocity) {
            velocity.add(gravitationalAcceleration);
            velocity.y *= 0.98;
            displacement.add(MovementSync.Instance.velocity.get());
            displacement.add(new Vector3d(gravitationalAcceleration).div(2).mul(0.98));
        } else if (velocity.y < 0) {
            velocity.y = terminalVelocity;
            displacement.add(MovementSync.Instance.velocity.get().add(velocity).div(2));
        }

        if(MovementSync.Instance.onGround.get()) {
            velocity.y = 0;
            displacement.y = 0;
        }
        Vector3d position = new Vector3d(MovementSync.Instance.position.get());
        position.add(displacement);
        MovementSync.Instance.velocity.set(velocity);
        MovementSync.Instance.position.set(position);

        if (!lastPos.equals(MovementSync.Instance.position.get()) && Bot.Instance.getServer() == Server.Xin) {
            syncPositionToServer();
        }
    }
}
