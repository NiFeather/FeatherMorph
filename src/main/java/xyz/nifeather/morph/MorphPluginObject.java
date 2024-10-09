package xyz.nifeather.morph;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import xiamomc.pluginbase.PluginObject;

public class MorphPluginObject extends PluginObject<MorphPlugin>
{
    @Override
    protected String getPluginNamespace()
    {
        return MorphPlugin.getMorphNameSpace();
    }

    protected void scheduleOn(Entity entity, Runnable r)
    {
        this.scheduleOn(entity, r, 1);
    }

    protected void scheduleOn(Entity entity, Runnable r, int delay)
    {
        entity.getScheduler().execute(plugin, r, null, delay);
    }

    protected void scheduleWorld(Entity entity, Runnable r)
    {
        Bukkit.getRegionScheduler().execute(plugin, entity.getLocation(), r);
    }

    public void dispose()
    {
    }
}
