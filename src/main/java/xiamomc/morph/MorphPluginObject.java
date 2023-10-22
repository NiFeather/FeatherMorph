package xiamomc.morph;

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
        entity.getScheduler().execute(plugin, r, null, 1);
    }

    protected void scheduleWorld(Entity entity, Runnable r)
    {
        Bukkit.getRegionScheduler().execute(plugin, entity.getLocation(), r);
    }
}
