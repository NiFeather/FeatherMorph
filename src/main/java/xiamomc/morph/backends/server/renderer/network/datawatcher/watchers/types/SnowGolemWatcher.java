package xiamomc.morph.backends.server.renderer.network.datawatcher.watchers.types;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import xiamomc.morph.backends.server.renderer.network.datawatcher.ValueIndex;

public class SnowGolemWatcher extends LivingEntityWatcher
{
    public SnowGolemWatcher(Player bindingPlayer)
    {
        super(bindingPlayer, EntityType.SNOWMAN);
    }

    @Override
    protected void initRegistry()
    {
        super.initRegistry();

        register(ValueIndex.SNOW_GOLEM);
    }
}
