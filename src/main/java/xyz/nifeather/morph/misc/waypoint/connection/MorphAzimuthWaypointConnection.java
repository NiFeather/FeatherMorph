package xyz.nifeather.morph.misc.waypoint.connection;

import net.minecraft.network.protocol.game.ClientboundTrackedWaypointPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.waypoints.Waypoint;
import net.minecraft.world.waypoints.WaypointTransmitter;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import xyz.nifeather.morph.misc.DisguiseState;
import xyz.nifeather.morph.misc.NmsRecord;

public class MorphAzimuthWaypointConnection implements WaypointTransmitter.Connection
{
    private final DisguiseState bindingState;
    private final Waypoint.Icon icon;
    private final ServerPlayer receiver;
    private final Player receiverBukkit;
    private float lastAngle;

    public MorphAzimuthWaypointConnection(DisguiseState state, Waypoint.Icon icon, ServerPlayer receiver)
    {
        this.bindingState = state;
        this.icon = icon;
        this.receiver = receiver;
        this.receiverBukkit = receiver.getBukkitEntity();
    }

    @Override
    public void connect()
    {
        var uuid = bindingState.getDisguiseWrapper().getVirtualEntityUUID();

        this.receiver.connection.send(ClientboundTrackedWaypointPacket.addWaypointAzimuth(uuid, this.icon, this.lastAngle));
    }

    @Override
    public void disconnect()
    {
        var uuid = bindingState.getDisguiseWrapper().getVirtualEntityUUID();

        this.receiver.connection.send(ClientboundTrackedWaypointPacket.removeWaypoint(uuid));
    }

    @Override
    public void update()
    {
        var player = bindingState.getPlayer();

        var vec = player.getLocation().subtract(receiverBukkit.getLocation());
        vec = new Location(vec.getWorld(), -vec.x(), vec.y(), vec.z());

        float angle = (float) Math.atan2(vec.z(), vec.x());

        if (Mth.abs(angle - this.lastAngle) > 0.008726646F)
        {
            var uuid = bindingState.getDisguiseWrapper().getVirtualEntityUUID();
            this.receiver.connection.send(ClientboundTrackedWaypointPacket.updateWaypointAzimuth(uuid, this.icon, angle));
            this.lastAngle = angle;
        }
    }

    @Override
    public boolean isBroken()
    {
        var source = NmsRecord.ofPlayer(bindingState.getPlayer());
        return !WaypointTransmitter.isReallyFar(source, receiver);
    }
}
