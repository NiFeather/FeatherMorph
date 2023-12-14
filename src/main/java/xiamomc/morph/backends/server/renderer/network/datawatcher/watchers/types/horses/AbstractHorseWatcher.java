package xiamomc.morph.backends.server.renderer.network.datawatcher.watchers.types.horses;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import xiamomc.morph.backends.server.renderer.network.datawatcher.ValueIndex;
import xiamomc.morph.backends.server.renderer.network.datawatcher.watchers.types.LivingEntityWatcher;

public class AbstractHorseWatcher extends LivingEntityWatcher
{
    @Override
    protected void initRegistry()
    {
        super.initRegistry();

        register(ValueIndex.ABSTRACT_HORSE);
    }

    public AbstractHorseWatcher(Player bindingPlayer, EntityType entityType)
    {
        super(bindingPlayer, entityType);
    }
}
