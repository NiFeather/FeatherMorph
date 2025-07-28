package xyz.nifeather.morph.misc.waypoint.connection;

//import net.minecraft.network.protocol.game.ClientboundTrackedWaypointPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
//import net.minecraft.world.waypoints.Waypoint;
//import net.minecraft.world.waypoints.WaypointTransmitter;
import org.bukkit.entity.Player;
import xyz.nifeather.morph.misc.DisguiseState;
import xyz.nifeather.morph.misc.NmsRecord;

public class MorphChunkConnection
{
}
/*
public class MorphChunkConnection implements WaypointTransmitter.Connection
{
    private final DisguiseState bindingState;
    private final Waypoint.Icon icon;
    private final ServerPlayer receiver;
    private final Player receiverBukkit;
    private ChunkPos lastPosition;

    public MorphChunkConnection(DisguiseState state, Waypoint.Icon icon, ServerPlayer receiver)
    {
        this.bindingState = state;
        this.icon = icon;
        this.receiver = receiver;
        this.receiverBukkit = receiver.getBukkitEntity();
        this.lastPosition = NmsRecord.ofPlayer(state.getPlayer()).chunkPosition();
    }

    @Override
    public void connect()
    {
        var uuid = bindingState.getDisguiseWrapper().getVirtualEntityUUID();
        var player = NmsRecord.ofPlayer(bindingState.getPlayer());
        this.receiver.connection.send(ClientboundTrackedWaypointPacket.addWaypointChunk(uuid, this.icon, player.chunkPosition()));
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
        var source = NmsRecord.ofPlayer(bindingState.getPlayer());
        ChunkPos chunkPos = source.chunkPosition();
        if (chunkPos.getChessboardDistance(this.lastPosition) <= 0)
            return;

        var uuid = bindingState.getDisguiseWrapper().getVirtualEntityUUID();
        this.receiver.connection.send(ClientboundTrackedWaypointPacket.updateWaypointChunk(uuid, this.icon, chunkPos));
        this.lastPosition = chunkPos;
    }

    @Override
    public boolean isBroken()
    {
        var player = NmsRecord.ofPlayer(bindingState.getPlayer());

        return !WaypointTransmitter.isChunkVisible(player.chunkPosition(), receiver)
                || WaypointTransmitter.isReallyFar(player, receiver)
                || bindingState.getPlayer().isTrackedBy(receiverBukkit);
    }
}
*/