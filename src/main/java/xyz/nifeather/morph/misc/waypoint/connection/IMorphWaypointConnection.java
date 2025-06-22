package xyz.nifeather.morph.misc.waypoint.connection;

import net.minecraft.world.waypoints.WaypointTransmitter;

public interface IMorphWaypointConnection extends WaypointTransmitter.Connection
{
    void internalUpdate();
}
