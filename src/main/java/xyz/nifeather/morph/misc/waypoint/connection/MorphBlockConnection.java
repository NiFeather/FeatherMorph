package xyz.nifeather.morph.misc.waypoint.connection;

import net.minecraft.core.Vec3i;
//import net.minecraft.network.protocol.game.ClientboundTrackedWaypointPacket;
import net.minecraft.server.level.ServerPlayer;
//import net.minecraft.world.waypoints.Waypoint;
//import net.minecraft.world.waypoints.WaypointTransmitter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import xyz.nifeather.morph.misc.DisguiseState;
import xyz.nifeather.morph.misc.NmsRecord;

public class MorphBlockConnection implements IMorphRealtimeWaypointConnection
{
}
/*
public class MorphBlockConnection implements IMorphWaypointConnection
{
    private final DisguiseState bindingState;
    private final Waypoint.Icon icon;
    private final ServerPlayer receiver;
    private final Player receiverBukkit;

    @NotNull
    private Vec3i lastPosition;

    public MorphBlockConnection(DisguiseState state,
                                Waypoint.Icon icon,
                                ServerPlayer receiver)
    {
        this.bindingState = state;
        this.icon = icon;
        this.receiver = receiver;
        this.receiverBukkit = receiver.getBukkitEntity();
        this.lastPosition = NmsRecord.ofPlayer(state.getPlayer()).blockPosition();
    }

    @Override
    public void connect()
    {
        var uuid = bindingState.getDisguiseWrapper().getVirtualEntityUUID();
        this.receiver.connection.send(ClientboundTrackedWaypointPacket.addWaypointPosition(uuid, this.icon, lastPosition));
    }

    @Override
    public void disconnect()
    {
        var uuid = bindingState.getDisguiseWrapper().getVirtualEntityUUID();
        this.receiver.connection.send(ClientboundTrackedWaypointPacket.removeWaypoint(uuid));
    }

    @Override
    public void internalUpdate()
    {
        var source = NmsRecord.ofPlayer(bindingState.getPlayer());
        var blockPos = source.blockPosition();

        if (blockPos.distManhattan(this.lastPosition) <= 0)
            return;

        var uuid = bindingState.getDisguiseWrapper().getVirtualEntityUUID();
        this.receiver.connection.send(ClientboundTrackedWaypointPacket.updateWaypointPosition(uuid, this.icon, blockPos));
        this.lastPosition = blockPos;
    }

    @Override
    public void update()
    {
        internalUpdate();
    }

    @Override
    public boolean isBroken()
    {
        var player = NmsRecord.ofPlayer(bindingState.getPlayer());

        return !bindingState.getPlayer().isTrackedBy(receiverBukkit)
                || !WaypointTransmitter.isChunkVisible(player.chunkPosition(), receiver);
    }
}
*/