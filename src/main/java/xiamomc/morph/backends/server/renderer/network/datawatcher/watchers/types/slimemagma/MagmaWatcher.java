package xiamomc.morph.backends.server.renderer.network.datawatcher.watchers.types.slimemagma;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

public class MagmaWatcher extends AbstractSlimeWatcher
{
    public MagmaWatcher(Player bindingPlayer)
    {
        super(bindingPlayer, EntityType.MAGMA_CUBE);
    }
}
