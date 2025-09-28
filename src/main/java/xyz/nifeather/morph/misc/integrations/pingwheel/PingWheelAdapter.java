package xyz.nifeather.morph.misc.integrations.pingwheel;

import org.bukkit.Bukkit;
import xyz.nifeather.morph.MorphPluginObject;

public class PingWheelAdapter extends MorphPluginObject
{
    public PingWheelAdapter()
    {
        Bukkit.getPluginManager().registerEvents(new PingWheelEventListener(), plugin);
        logger.info("Ok registered PingWheel handle!");
    }
}
