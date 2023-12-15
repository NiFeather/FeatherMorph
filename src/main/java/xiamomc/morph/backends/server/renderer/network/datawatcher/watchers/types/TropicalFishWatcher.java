package xiamomc.morph.backends.server.renderer.network.datawatcher.watchers.types;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import xiamomc.morph.backends.server.renderer.network.datawatcher.ValueIndex;

public class TropicalFishWatcher extends LivingEntityWatcher
{
    public TropicalFishWatcher(Player bindingPlayer)
    {
        super(bindingPlayer, EntityType.TROPICAL_FISH);
    }

    @Override
    protected void initRegistry()
    {
        super.initRegistry();

        register(ValueIndex.TROPICAL);
    }

    @Override
    protected void initValues()
    {
        super.initValues();

        //todo
    }
}
