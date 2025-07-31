package xyz.nifeather.morph.misc.waypoint.connection;

import net.minecraft.world.waypoints.WaypointTransmitter;

public interface IMorphRealtimeWaypointConnection extends WaypointTransmitter.Connection
{
    void internalUpdate();
}
