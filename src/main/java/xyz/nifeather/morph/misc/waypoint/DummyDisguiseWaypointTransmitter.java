package xyz.nifeather.morph.misc.waypoint;

import net.minecraft.server.level.ServerPlayer;

import java.util.Optional;

public class DummyDisguiseWaypointTransmitter implements IDisguiseWaypointTransmitter
{
    private final String playerName;

    public DummyDisguiseWaypointTransmitter(String playerName)
    {
        this.playerName = playerName;
    }

    @Override
    public void tick()
    {
    }

    @Override
    public void updateRealtimeConnections()
    {
    }

    @Override
    public void setEnabled(boolean enabled)
    {
    }

    @Override
    public void dispose()
    {
    }

    @Override
    public boolean isTransmittingWaypoint()
    {
        return false;
    }

    @Override
    public Optional<Connection> makeWaypointConnectionWith(ServerPlayer serverPlayer)
    {
        return Optional.empty();
    }

    private final Icon waypointIcon = new Icon();

    @Override
    public Icon waypointIcon()
    {
        return waypointIcon;
    }

    @Override
    public String toString()
    {
        return "(Dummy Disguise Waypoint for %s)@%s".formatted(playerName, Integer.toHexString(hashCode()));
    }
}
