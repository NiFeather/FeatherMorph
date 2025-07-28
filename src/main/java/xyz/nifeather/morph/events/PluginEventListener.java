package xyz.nifeather.morph.events;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import xyz.nifeather.morph.MorphPluginObject;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public class PluginEventListener extends MorphPluginObject implements Listener
{
    @EventHandler
    public void onPluginDisable(PluginDisableEvent e)
    {
        onDisableConsumers.forEach(c -> c.accept(e.getPlugin().getName()));
    }

    @EventHandler
    public void onPluginEnable(PluginEnableEvent e)
    {
        onEnableConsumers.forEach(c -> c.accept(e.getPlugin().getName()));
    }

    private final List<Consumer<String>> onEnableConsumers = new CopyOnWriteArrayList<>();
    private final List<Consumer<String>> onDisableConsumers = new CopyOnWriteArrayList<>();

    public void onPluginEnable(Consumer<String> c)
    {
        onEnableConsumers.add(c);
    }

    public void onPluginDisable(Consumer<String> c)
    {
        onDisableConsumers.add(c);
    }
}
