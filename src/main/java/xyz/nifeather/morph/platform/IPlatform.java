package xyz.nifeather.morph.platform;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;

public interface IPlatform
{
    /**
     * Get currently online players
     */
    @Unmodifiable
    public List<Player> onlinePlayers();

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
    public void runAtEntityDelayed(Entity entity, Runnable r, int delay);

    /**
     * @param delay Any number greater than zero.
     */
    public void runAtLocationDelayed(Location location, Runnable r, int delay);
}
