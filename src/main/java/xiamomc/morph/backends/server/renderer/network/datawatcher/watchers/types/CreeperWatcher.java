package xiamomc.morph.backends.server.renderer.network.datawatcher.watchers.types;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import xiamomc.morph.backends.server.renderer.network.registries.ValueIndex;

public class CreeperWatcher extends LivingEntityWatcher
{
    public CreeperWatcher(Player bindingPlayer)
    {
        super(bindingPlayer, EntityType.CREEPER);
    }

    @Override
    protected void initRegistry()
    {
        super.initRegistry();

        register(ValueIndex.CREEPER);
    }
}
