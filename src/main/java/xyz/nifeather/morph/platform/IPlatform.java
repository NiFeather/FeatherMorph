package xyz.nifeather.morph.platform;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.Unmodifiable;
import xyz.nifeather.morph.platform.entity.IPlatformEntity;
import xyz.nifeather.morph.platform.entity.IPlatformEntityLookup;
import xyz.nifeather.morph.platform.entity.IPlatformPlayer;
import xyz.nifeather.morph.platform.world.IPlatformWorldLookup;

import java.util.List;

public interface IPlatform<TNativeEntity, TNativePlayer, TNativeWorld, TNativeLocation>
{
    /**
     * Get currently online players
     */
    @Unmodifiable
    public List<TNativePlayer> onlinePlayersNative();

    /**
     * Get currently online players
     */
    @Unmodifiable
    public List<IPlatformPlayer> onlinePlayers();

    public IPlatformEntityLookup<TNativeEntity, TNativePlayer> entityLookup();

    public IPlatformWorldLookup<TNativeWorld, TNativeLocation> worldLookup();

    /**
     * @param delay Any number greater than zero.
     */
    public void runDelayed(Runnable r, int delay);

    /**
     * @param delay If zero, should run as soon as possible.
     */
    public void runAsyncDelayed(Runnable r, int delay);

    /**
     * @param delay Any number greater than zero.
     */
    public void runAtEntityDelayedNative(Entity entity, Runnable r, int delay);

    /**
     * @param delay Any number greater than zero.
     */
    public void runAtEntityDelayed(IPlatformEntity entity, Runnable r, int delay);

    /**
     * @param delay Any number greater than zero.
     */
    public void runAtLocationDelayedNative(Location location, Runnable r, int delay);
}
