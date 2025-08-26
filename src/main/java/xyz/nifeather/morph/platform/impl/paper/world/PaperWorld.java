package xyz.nifeather.morph.platform.impl.paper.world;

import org.bukkit.Difficulty;
import org.bukkit.World;
import xyz.nifeather.morph.platform.world.IPlatformWorld;
import xyz.nifeather.morph.platform.world.WorldDifficulty;

public class PaperWorld implements IPlatformWorld
{
    private final World handle;

    public PaperWorld(World handle)
    {
        this.handle = handle;
    }

    @Override
    public WorldDifficulty difficulty()
    {
        return fromBukkitDifficulty(handle.getDifficulty());
    }

    private WorldDifficulty fromBukkitDifficulty(Difficulty difficulty)
    {
        return switch (difficulty)
        {
            case PEACEFUL -> WorldDifficulty.PEACEFUL;
            case EASY -> WorldDifficulty.EASY;
            case NORMAL -> WorldDifficulty.NORMAL;
            case HARD -> WorldDifficulty.HARD;
            default -> throw new RuntimeException("Bad server implementation? Invalid difficulty " + difficulty);
        };
    }

    public World getHandle()
    {
        return handle;
    }
}
