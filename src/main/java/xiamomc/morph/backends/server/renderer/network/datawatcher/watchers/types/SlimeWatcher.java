package xiamomc.morph.backends.server.renderer.network.datawatcher.watchers.types;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import xiamomc.morph.backends.server.renderer.network.datawatcher.ValueIndex;

public class SlimeWatcher extends LivingEntityWatcher
{
    public SlimeWatcher(Player bindingPlayer)
    {
        super(bindingPlayer, EntityType.SLIME);

        register(ValueIndex.SLIME_MAGMA);
    }
}
