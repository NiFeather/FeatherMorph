package xiamomc.morph.backends.server.renderer.network.datawatcher.watchers.types;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import xiamomc.morph.backends.server.renderer.network.datawatcher.ValueIndex;

public class AllayWatcher extends LivingEntityWatcher
{
    @Override
    protected void initRegistry()
    {
        super.initRegistry();

        register(ValueIndex.ALLAY);
    }

    public AllayWatcher(Player bindingPlayer)
    {
        super(bindingPlayer, EntityType.ALLAY);
    }

    @Override
    protected void doSync()
    {
        super.doSync();
    }
}
