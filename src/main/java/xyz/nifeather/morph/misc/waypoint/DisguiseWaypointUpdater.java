package xyz.nifeather.morph.misc.waypoint;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import net.minecraft.world.waypoints.WaypointTransmitter;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.nifeather.morph.misc.DisguiseState;
import xyz.nifeather.morph.misc.NmsRecord;
import xyz.nifeather.morph.misc.waypoint.connection.*;
import xyz.nifeather.morph.providers.disguise.DefaultDisguiseProvider;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class DisguiseWaypointUpdater implements WaypointTransmitter
{
    public DisguiseWaypointUpdater(DisguiseState state)
    {
        this.bindingState = state;
    }

    private volatile boolean transmitting;
    private volatile boolean disposed;

    public void dispose()
    {
        enabled(false);
        tick();

        disposed = true;
    }

    @Nullable
    private ServerLevel lastWorld = null;

    public void tick()
    {
        var allowConnection = allowWaypointConnection();

        var currentWorld = NmsRecord.ofPlayer(getPlayer()).level();
        var currentWaypointManager = currentWorld.getWaypointManager();

        if (!currentWorld.equals(lastWorld))
        {
            if (lastWorld != null)
                lastWorld.getWaypointManager().untrackWaypoint(this);

            transmitting = false;
        }

        lastWorld = currentWorld;

        if (transmitting != allowConnection)
        {
            if (allowConnection)
            {
                if (!currentWaypointManager.transmitters().contains(this))
                    currentWaypointManager.trackWaypoint(this);

                transmitting = true;
            }
            else
            {
                transmitting = false;

                currentWaypointManager.untrackWaypoint(this);
                this.realtimeConnections.clear();
            }
        }
    }

    private volatile boolean enabled = false;

    public boolean allowWaypointConnection()
    {
        if (!enabled) return false;

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
                var value = playerAttribute.getValue(); // Gets the value of this attribute without our modifier
                playerAttribute.addTransientModifier(modifier);

                return value > 0d;
            }
        }

        return true;
    }

    public void enabled(boolean value)
    {
        if (disposed) return;

        this.enabled = value;
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
