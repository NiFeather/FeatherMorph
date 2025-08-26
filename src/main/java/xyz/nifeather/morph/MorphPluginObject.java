package xyz.nifeather.morph;

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
        featherMorph().getPlatform().runAtEntityDelayedNative(entity, r, delay);
    }

    protected void scheduleAt(Location location, Runnable r)
    {
        scheduleAt(location, r, 1);
    }

    protected void scheduleAt(Location location, Runnable r, int delay)
    {
        featherMorph().getPlatform().runAtLocationDelayedNative(location, r, delay);
    }

    public void dispose()
    {
    }
}
