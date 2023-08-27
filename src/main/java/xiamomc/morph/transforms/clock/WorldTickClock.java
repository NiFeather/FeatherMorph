package xiamomc.morph.transforms.clock;

import org.bukkit.Bukkit;
import org.bukkit.World;

public class WorldTickClock implements IClock
{
    private final World bindingWorld;

    public WorldTickClock(World world)
    {
        this.bindingWorld = world;
    }

    @Override
    public long getCurrentTimeMills()
    {
        return bindingWorld.getTime() * 50;
    }

    @Override
    public long getCurrentTimeTicks()
    {
        return bindingWorld.getTime();
    }
}
