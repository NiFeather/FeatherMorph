package xyz.nifeather.morph.platform.impl.paper.world;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.util.concurrent.ExecutionError;
import com.google.common.util.concurrent.UncheckedExecutionException;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import xyz.nifeather.morph.FeatherMorphMain;
import xyz.nifeather.morph.platform.world.IPlatformLocation;
import xyz.nifeather.morph.platform.world.IPlatformWorld;
import xyz.nifeather.morph.platform.world.IPlatformWorldLookup;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

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

    private final Cache<World, PaperWorld> worldCache = CacheBuilder.newBuilder()
            .concurrencyLevel(4)
            .expireAfterAccess(Duration.ofMinutes(1))
            .build();

    @Override
    public PaperWorld getPlatformWorld(World world)
    {
        try
        {
            return worldCache.get(world, () -> new PaperWorld(world));
        }
        catch (ExecutionException | ExecutionError | UncheckedExecutionException e)
        {
            FeatherMorphMain.getInstance().getSLF4JLogger().error("Failed fetching cross-platform world from cache, returning new instance.", e);
        }

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
