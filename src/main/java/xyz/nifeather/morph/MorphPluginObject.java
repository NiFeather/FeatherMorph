package xyz.nifeather.morph;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import xiamomc.pluginbase.PluginObject;

public class MorphPluginObject extends PluginObject<FeatherMorphMain>
{
    @Override
    protected String getPluginNamespace()
    {
        return FeatherMorphMain.getMorphNameSpace();
    }

    public FeatherMorphMain featherMorph()
    {
        return (FeatherMorphMain) plugin;
    }

    protected void scheduleOn(Entity entity, Runnable r)
    {
        this.scheduleOn(entity, r, 1);
    }

    protected void scheduleOn(Entity entity, Runnable r, int delay)
    {
        entity.getScheduler().execute(plugin, r, null, delay);
    }

    protected void scheduleAt(Location location, Runnable r)
    {
        Bukkit.getRegionScheduler().execute(plugin, location, r);
    }

    public void dispose()
    {
    }
}
