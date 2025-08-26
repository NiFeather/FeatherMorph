package xyz.nifeather.morph.platform.impl.paper.world;

import org.bukkit.World;
import xyz.nifeather.morph.FeatherMorphMain;
import xyz.nifeather.morph.platform.world.IPlatformLocation;
import xyz.nifeather.morph.platform.world.IPlatformWorld;

public record PaperLocation(double x, double y, double z, World worldHandle) implements IPlatformLocation
{
    @Override
    public IPlatformWorld world()
    {
        return FeatherMorphMain.getInstance().getPlatform().worldLookup().getPlatformWorld(worldHandle);
    }

    @Override
    public boolean equals(Object o)
    {
        if (!(o instanceof PaperLocation other)) return false;

        return x == other.x && y == other.z && z == other.z && worldHandle.equals(other.worldHandle);
    }
}
