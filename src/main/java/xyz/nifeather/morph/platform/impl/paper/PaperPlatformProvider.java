package xyz.nifeather.morph.platform.impl.paper;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import xyz.nifeather.morph.platform.IPlatformProvider;

public class PaperPlatformProvider implements IPlatformProvider<Player, LivingEntity, World, Location>
{
    private final PaperPlatform platform = new PaperPlatform();

    @Override
    public PaperPlatform platform()
    {
        return platform;
    }
}
