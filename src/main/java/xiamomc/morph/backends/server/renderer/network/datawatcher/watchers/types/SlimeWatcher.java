package xiamomc.morph.backends.server.renderer.network.datawatcher.watchers.types;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import xiamomc.morph.backends.server.renderer.network.datawatcher.ValueIndex;

public class SlimeWatcher extends LivingEntityWatcher
{
    @Override
    protected void initRegistry()
    {
        super.initRegistry();

        register(ValueIndex.SLIME_MAGMA);
    }

    public SlimeWatcher(Player bindingPlayer)
    {
        super(bindingPlayer, EntityType.SLIME);
    }
}
