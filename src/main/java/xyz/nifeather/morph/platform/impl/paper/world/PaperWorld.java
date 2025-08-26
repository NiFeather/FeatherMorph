package xyz.nifeather.morph.platform.impl.paper.world;

import org.bukkit.World;
import xyz.nifeather.morph.platform.world.IPlatformWorld;

public class PaperWorld implements IPlatformWorld
{
    private final World handle;

    public PaperWorld(World handle)
    {
        this.handle = handle;
    }

    public World getHandle()
    {
        return handle;
    }
}
