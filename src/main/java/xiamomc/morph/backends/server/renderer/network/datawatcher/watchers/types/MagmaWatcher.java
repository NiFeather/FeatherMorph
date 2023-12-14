package xiamomc.morph.backends.server.renderer.network.datawatcher.watchers.types;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import xiamomc.morph.backends.server.renderer.network.datawatcher.ValueIndex;

public class MagmaWatcher extends LivingEntityWatcher
{
    public MagmaWatcher(Player bindingPlayer)
    {
        super(bindingPlayer, EntityType.MAGMA_CUBE);

        register(ValueIndex.SLIME_MAGMA);
    }
}
