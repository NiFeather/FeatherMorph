package xyz.nifeather.morph.platform.impl.paper.world;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import xyz.nifeather.morph.platform.world.IPlatformLocation;
import xyz.nifeather.morph.platform.world.IPlatformWorld;
import xyz.nifeather.morph.platform.world.IPlatformWorldLookup;

import java.util.UUID;

public class PaperWorldLookup implements IPlatformWorldLookup<World, Location>
{
    @Override
    public PaperWorld lookup(String name)
    {
        var nativeWorld = Bukkit.getWorld(name);
        return getPlatformWorld(nativeWorld);
    }

    @Override
    public PaperWorld lookup(UUID uuid)
    {
        var nativeWorld = Bukkit.getWorld(uuid);
        return getPlatformWorld(nativeWorld);
    }

    @Override
    public PaperWorld getPlatformWorld(World world)
    {
        return new PaperWorld(world);
    }

    @Override
    public World getNativeWorld(IPlatformWorld platformWorld)
    {
        return ((PaperWorld)platformWorld).getHandle();
    }

    @Override
    public PaperLocation getPlatformLocation(Location location)
    {
        return new PaperLocation(location.x(), location.y(), location.z(), location.getWorld());
    }

    @Override
    public Location getNativeLocation(IPlatformLocation loc)
    {
        var worldHandle = ((PaperLocation) loc).worldHandle();
        return new Location(worldHandle, loc.x(), loc.y(), loc.z());
    }
}
