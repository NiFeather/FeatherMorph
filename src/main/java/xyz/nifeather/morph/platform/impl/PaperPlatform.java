package xyz.nifeather.morph.platform.impl;

import com.google.common.collect.ImmutableList;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import xyz.nifeather.morph.FeatherMorphMain;
import xyz.nifeather.morph.platform.IPlatform;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class PaperPlatform implements IPlatform
{
    private FeatherMorphMain plugin()
    {
        return FeatherMorphMain.getInstance();
    }

    @Override
    public List<Player> onlinePlayers()
    {
        return ImmutableList.copyOf(Bukkit.getOnlinePlayers());
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
    public void runAtEntityDelayed(Entity entity, Runnable r, int delay)
    {
        delay = Math.max(1, delay);

        entity.getScheduler()
                .runDelayed(plugin(), task -> r.run(), null, delay);
    }

    @Override
    public void runAtLocationDelayed(Location location, Runnable r, int delay)
    {
        delay = Math.max(1, delay);

        Bukkit.getRegionScheduler()
                .runDelayed(plugin(), location, task -> r.run(), delay);
    }
}
