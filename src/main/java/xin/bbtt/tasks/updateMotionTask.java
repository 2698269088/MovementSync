package xin.bbtt.tasks;

import org.geysermc.mcprotocollib.protocol.packet.ingame.serverbound.player.ServerboundMovePlayerPosPacket;
import org.joml.Vector3d;
import xin.bbtt.MovementSync;
import xin.bbtt.mcbot.Bot;
import xin.bbtt.mcbot.Server;
import xin.bbtt.world.Direction;

import static xin.bbtt.MovementSync.gravitationalAcceleration;
import static xin.bbtt.MovementSync.terminalVelocity;
import static xin.bbtt.world.World.isOnGround;

public class updateMotionTask implements Runnable {

    public void syncPositionToServer() {
        Bot.Instance.getSession().send(new ServerboundMovePlayerPosPacket(MovementSync.Instance.onGround.get(), MovementSync.Instance.position.get().x, MovementSync.Instance.position.get().y, MovementSync.Instance.position.get().z));
        MovementSync.Instance.getLogger().info("Synced position to server: ({}, {}, {}, {}), vertical velocity: {}b/t", MovementSync.Instance.onGround, MovementSync.Instance.position.get().x, MovementSync.Instance.position.get().y, MovementSync.Instance.position.get().z, MovementSync.Instance.velocity.get().y);
    }

    public void checkOnGround() {
        Vector3d position = new Vector3d(MovementSync.Instance.position.get());
        MovementSync.Instance.onGround.set(isOnGround(position));
    }

    @Override
    public void run() {
        if (!Bot.Instance.isRunning()) return;
        if (Bot.Instance.getServer() != Server.Xin) return;
        Vector3d lastPos = new Vector3d(MovementSync.Instance.position.get());
        Vector3d velocity = MovementSync.Instance.velocity.get();
        Vector3d displacement = new Vector3d();

        checkOnGround();
        if(MovementSync.Instance.onGround.get()) {
            velocity.y = 0;
            displacement.y = 0;
        }

        if (velocity.y > terminalVelocity) {
            if (!MovementSync.Instance.onGround.get()) velocity.add(gravitationalAcceleration);
            velocity.y *= 0.9800000190734863D;
            displacement.add(MovementSync.Instance.velocity.get());
            displacement.add(new Vector3d(gravitationalAcceleration).div(2));
        } else if (velocity.y < 0) {
            velocity.y = terminalVelocity;
            displacement.add(MovementSync.Instance.velocity.get().add(velocity).div(2));
        }
        Vector3d position = new Vector3d(MovementSync.Instance.position.get());
        Vector3d lowest = new Vector3d(MovementSync.Instance.position.get()).add(Direction.DOWN.getUnitVector());

        if (!MovementSync.Instance.onGround.get()) {
            while (!isOnGround(lowest))
                lowest.add(Direction.DOWN.getUnitVector());
        }

        position.add(displacement);

        if (position.y < lowest.y){
            position.y = lowest.y;
        }

        MovementSync.Instance.velocity.set(velocity);
        MovementSync.Instance.position.set(position);

        if (!lastPos.equals(MovementSync.Instance.position.get()) && Bot.Instance.getServer() == Server.Xin) {
            syncPositionToServer();
        }
    }
}
