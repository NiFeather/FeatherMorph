package xiamomc.morph.backends.server.renderer.network.datawatcher.watchers.types;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import xiamomc.morph.backends.server.renderer.network.datawatcher.ValueIndex;

public class AllayWatcher extends LivingEntityWatcher
{
    public AllayWatcher(Player bindingPlayer)
    {
        super(bindingPlayer, EntityType.ALLAY);

        register(ValueIndex.ALLAY);
    }

    @Override
    public void doSync()
    {
        write(ValueIndex.ALLAY.DANCING, true);
        super.doSync();
    }
}
