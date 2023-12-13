package xiamomc.morph.backends.server.renderer.network.datawatcher.watchers.types;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import xiamomc.morph.backends.server.renderer.network.datawatcher.ValueIndex;

public class ArmorStandWatcher extends LivingEntityWatcher
{
    public ArmorStandWatcher(Player bindingPlayer)
    {
        super(bindingPlayer, EntityType.ARMOR_STAND);

        register(ValueIndex.ARMOR_STAND);
    }

    @Override
    protected void doSync()
    {
        super.doSync();
    }
}
