package xyz.nifeather.morph.misc.waypoint;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.waypoints.WaypointTransmitter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import xiamomc.pluginbase.Exceptions.NullDependencyException;
import xyz.nifeather.morph.FeatherMorphMain;
import xyz.nifeather.morph.misc.DisguiseState;
import xyz.nifeather.morph.misc.NmsRecord;
import xyz.nifeather.morph.misc.waypoint.connection.IMorphWaypointConnection;
import xyz.nifeather.morph.misc.waypoint.connection.MorphAzimuthWaypointConnection;
import xyz.nifeather.morph.misc.waypoint.connection.MorphBlockConnection;
import xyz.nifeather.morph.misc.waypoint.connection.MorphChunkConnection;

import java.util.List;
import java.util.Optional;

public class WaypointUpdater implements WaypointTransmitter
{
    public WaypointUpdater(DisguiseState state)
    {
        this.bindingState = state;
    }

    private boolean allowWaypoint = true;

    public boolean allowWaypointConnection()
    {
        return allowWaypoint;
    }

    public void allowWaypointConnection(boolean allow)
    {
        this.allowWaypoint = allow;
    }

    private final DisguiseState bindingState;

    @NotNull
    public Player getPlayer() throws NullDependencyException
    {
        return bindingState.getPlayer();
    }

    @Override
    public boolean isTransmittingWaypoint()
    {
        return true;
    }

    private final List<IMorphWaypointConnection> realtimeConnections = new ObjectArrayList<>();

    public void updateRealtimeConnections()
    {
        synchronized (realtimeConnections)
        {
            var list = List.copyOf(realtimeConnections);

            for (var connection : list)
            {
                if (connection.isBroken())
                    realtimeConnections.remove(connection);
                else
                    connection.internalUpdate();
            }
        }
    }

    @Override
    public Optional<Connection> makeWaypointConnectionWith(ServerPlayer target)
    {
        if (!allowWaypoint)
            return Optional.empty();

        var player = NmsRecord.ofPlayer(getPlayer());

        if (player == target)
            return Optional.empty();

        var icon = waypointIcon();

        FeatherMorphMain.getInstance().getSLF4JLogger().info("Getting new instance");

        if (WaypointTransmitter.isReallyFar(player, target))
        {
            return Optional.of(new MorphAzimuthWaypointConnection(bindingState, icon, target));
        }
        else
        {
            var targetBukkit = target.getBukkitEntity();
            if (!bindingState.getPlayer().isTrackedBy(targetBukkit))
            {
                return Optional.of(new MorphChunkConnection(bindingState, icon, target));
            }
            else
            {
                var conn = new MorphBlockConnection(bindingState, icon, target);

                synchronized (realtimeConnections)
                {
                    realtimeConnections.add(conn);
                }

                return Optional.of(conn);
            }
        }
    }

    /**
     * We might want to support changing icon in the future.
     */
    private final Icon waypointIcon = new Icon();

    @Override
    public Icon waypointIcon()
    {
        return waypointIcon;
    }

    @Override
    public String toString()
    {
        var pl = bindingState.getPlayer();
        String playerString = pl.getName();
        return "(Disguise Waypoint for %s)@%s".formatted(playerString, Integer.toHexString(hashCode()));
    }
}
