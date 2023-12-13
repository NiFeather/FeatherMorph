package xiamomc.morph.backends.server.renderer.network.datawatcher.watchers.types;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import xiamomc.morph.backends.server.renderer.network.datawatcher.ValueIndex;
import xiamomc.morph.misc.NmsRecord;

public class LivingEntityWatcher extends EntityWatcher
{
    public LivingEntityWatcher(Player bindingPlayer, EntityType entityType)
    {
        super(bindingPlayer, entityType);

        register(ValueIndex.BASE_LIVING);
    }

    @Override
    public void doSync()
    {
        var player = getBindingPlayer();
        //var nmsPlayer = NmsRecord.ofPlayer(player);
        var values = ValueIndex.BASE_LIVING;

        write(values.HEALTH, (float)player.getHealth());

        super.doSync();
    }
}
