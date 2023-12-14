package xiamomc.morph.backends.server.renderer.network.datawatcher.watchers.types.horses;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import xiamomc.morph.backends.server.renderer.network.datawatcher.ValueIndex;

public class HorseWatcher extends AbstractHorseWatcher
{
    public HorseWatcher(Player bindingPlayer)
    {
        super(bindingPlayer, EntityType.HORSE);
    }

    @Override
    protected void initRegistry()
    {
        super.initRegistry();

        register(ValueIndex.HORSE);
    }
}
