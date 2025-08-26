package xyz.nifeather.morph.platform.impl.paper;

import com.google.common.collect.ImmutableList;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Unmodifiable;
import xyz.nifeather.morph.FeatherMorphMain;
import xyz.nifeather.morph.misc.CacheWithDefault;
import xyz.nifeather.morph.platform.IPlatform;
import xyz.nifeather.morph.platform.entity.IPlatformEntity;
import xyz.nifeather.morph.platform.entity.IPlatformEntityLookup;
import xyz.nifeather.morph.platform.entity.IPlatformPlayer;
import xyz.nifeather.morph.platform.impl.paper.entity.*;
import xyz.nifeather.morph.platform.impl.paper.world.PaperWorldLookup;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class PaperPlatform implements IPlatform<LivingEntity, Player, World, Location>
{
    private FeatherMorphMain plugin()
    {
        return FeatherMorphMain.getInstance();
    }

    @Override
    public List<Player> onlinePlayersNative()
    {
        return ImmutableList.copyOf(Bukkit.getOnlinePlayers());
    }

    private IPlatformPlayer toPlatformPlayer(Player bukkitPlayer)
    {
        return entityLookup.getPlatformPlayer(bukkitPlayer);
    }

    private final CacheWithDefault<List<Player>> nativeCachedOnlinePlayers = new CacheWithDefault<>(List.of());
    private final CacheWithDefault<List<IPlatformPlayer>> cachedOnlinePlayers = new CacheWithDefault<>(List.of());

    @Override
    public @Unmodifiable List<IPlatformPlayer> onlinePlayers()
    {
        var nativeOnline = onlinePlayersNative();
        if (nativeOnline.hashCode() == nativeCachedOnlinePlayers.get().hashCode())
            return cachedOnlinePlayers.get();

        var newList = nativeOnline.stream().map(this::toPlatformPlayer).toList();
        this.nativeCachedOnlinePlayers.set(nativeOnline);
        this.cachedOnlinePlayers.set(newList);

        return newList;
    }

    private final PaperEntityLookup entityLookup = new PaperEntityLookup();

    @Override
    public IPlatformEntityLookup<LivingEntity, Player> entityLookup()
    {
        return entityLookup;
    }

    private final PaperWorldLookup worldLookup = new PaperWorldLookup();

    @Override
    public PaperWorldLookup worldLookup()
    {
        return worldLookup;
    }

    @Override
    public void runDelayed(Runnable r, int delay)
    {
        delay = Math.max(1, delay);

        Bukkit.getGlobalRegionScheduler()
                .runDelayed(plugin(), task -> r.run(), delay);
    }

    @Override
    public void runAsyncDelayed(Runnable r, int delay)
    {
        if (delay > 0)
            Bukkit.getAsyncScheduler().runDelayed(plugin(), task -> r.run(), delay, TimeUnit.MILLISECONDS);
        else
            Bukkit.getAsyncScheduler().runNow(plugin(), task -> r.run());
    }

    @Override
    public void runAtEntityDelayedNative(Entity entity, Runnable r, int delay)
    {
        delay = Math.max(1, delay);

        entity.getScheduler()
                .runDelayed(plugin(), task -> r.run(), null, delay);
    }

    @Override
    public void runAtEntityDelayed(IPlatformEntity entity, Runnable r, int delay)
    {
        this.runAtEntityDelayedNative(((PaperEntity)entity).getHandle(), r, delay);
    }

    @Override
    public void runAtLocationDelayedNative(Location location, Runnable r, int delay)
    {
        delay = Math.max(1, delay);

        Bukkit.getRegionScheduler()
                .runDelayed(plugin(), location, task -> r.run(), delay);
    }
}
