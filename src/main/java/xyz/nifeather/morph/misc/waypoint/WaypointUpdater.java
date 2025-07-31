package xyz.nifeather.morph.misc.waypoint;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import net.minecraft.world.waypoints.WaypointTransmitter;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import xyz.nifeather.morph.misc.DisguiseState;
import xyz.nifeather.morph.misc.NmsRecord;
import xyz.nifeather.morph.misc.waypoint.connection.IMorphRealtimeWaypointConnection;
import xyz.nifeather.morph.misc.waypoint.connection.MorphAzimuthWaypointConnection;
import xyz.nifeather.morph.misc.waypoint.connection.MorphBlockConnection;
import xyz.nifeather.morph.misc.waypoint.connection.MorphChunkConnection;
import xyz.nifeather.morph.providers.disguise.DefaultDisguiseProvider;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class WaypointUpdater implements WaypointTransmitter
{
    public WaypointUpdater(DisguiseState state)
    {
        this.bindingState = state;
    }

    private volatile boolean transmitting;

    public void tick()
    {
        var allowConnection = allowWaypointConnection();

        if (transmitting != allowConnection)
        {
            transmitting = allowConnection;
            var waypointManager = NmsRecord.ofPlayer(getPlayer()).level().getWaypointManager();

            if (allowConnection)
            {
                waypointManager.trackWaypoint(this);
                this.updateRealtimeConnections();
            }
            else
            {
                waypointManager.untrackWaypoint(this);
                this.realtimeConnections.clear();
            }
        }
    }

    private volatile boolean allowWaypoint = true;

    public boolean allowWaypointConnection()
    {
        if (!allowWaypoint) return false;

        if (bindingState.disposed()) return false;

        var player = bindingState.getPlayer();

        // See https://zh.minecraft.wiki/w/%E5%AE%9A%E4%BD%8D%E6%A0%8F
        if (player.isSneaking())
            return false;

        if (!player.isConnected())
            return false;

        if (player.hasPotionEffect(PotionEffectType.INVISIBILITY))
            return false;

        var playerAttribute = player.getAttribute(Attribute.WAYPOINT_TRANSMIT_RANGE);
        if (playerAttribute != null)
        {
            var modifier = playerAttribute.getModifier(DefaultDisguiseProvider.WAYPOINT_TRANSMIT_MODIFIER_KEY);
            if (modifier != null)
            {
                playerAttribute.removeModifier(modifier);
                var value = playerAttribute.getValue();
                playerAttribute.addModifier(modifier);

                return value > 0d;
            }
        }

        return true;
    }

    public void allowWaypointConnection(boolean allow)
    {
        this.allowWaypoint = allow;
    }

    private final DisguiseState bindingState;

    @NotNull
    public Player getPlayer()
    {
        return bindingState.getPlayer();
    }

    // This method seems to only get called for entities, not custom transmitter implementations.
    @Override
    public boolean isTransmittingWaypoint()
    {
        return transmitting;
    }

    private final List<IMorphRealtimeWaypointConnection> realtimeConnections = Collections.synchronizedList(new ObjectArrayList<>());

    public void updateRealtimeConnections()
    {
        if (!transmitting) return;

        synchronized (realtimeConnections)
        {
            var list = List.copyOf(realtimeConnections);

            for (var connection : list)
            {
                if (connection.isBroken())
                    realtimeConnections.remove(connection);
                else
                    connection.internalUpdate(); // We emulate the behavior in Entity#setPosRaw(double x, double y, double z, boolean forceBoundingBoxUpdate)
            }
        }
    }

    @Override
    public Optional<Connection> makeWaypointConnectionWith(ServerPlayer target)
    {
        if (!transmitting)
            return Optional.empty();

        var player = NmsRecord.ofPlayer(getPlayer());

        // See https://zh.minecraft.wiki/w/%E5%AE%9A%E4%BD%8D%E6%A0%8F
        if (player.gameMode() == GameType.SPECTATOR && target.gameMode() != GameType.SPECTATOR)
            return Optional.empty();

        if (player.equals(target))
            return Optional.empty();

        var icon = waypointIcon();

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
