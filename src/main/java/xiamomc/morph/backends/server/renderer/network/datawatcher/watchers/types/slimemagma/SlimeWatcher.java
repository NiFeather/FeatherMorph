package xiamomc.morph.backends.server.renderer.network.datawatcher.watchers.types.slimemagma;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

public class SlimeWatcher extends AbstractSlimeWatcher
{
    public SlimeWatcher(Player bindingPlayer)
    {
        super(bindingPlayer, EntityType.SLIME);
    }
}
