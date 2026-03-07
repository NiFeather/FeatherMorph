package xyz.nifeather.morph.misc.waypoint;

import net.minecraft.world.waypoints.WaypointTransmitter;

public interface IDisguiseWaypointTransmitter extends WaypointTransmitter
{
    void tick();

    void updateRealtimeConnections();

    void setEnabled(boolean enabled);

    void dispose();
}
