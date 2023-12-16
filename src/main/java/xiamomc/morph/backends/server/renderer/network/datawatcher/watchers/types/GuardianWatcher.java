package xiamomc.morph.backends.server.renderer.network.datawatcher.watchers.types;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import xiamomc.morph.backends.server.renderer.network.datawatcher.ValueIndex;

public class GuardianWatcher extends LivingEntityWatcher
{
    public GuardianWatcher(Player bindingPlayer)
    {
        super(bindingPlayer, EntityType.GUARDIAN);
    }

    protected GuardianWatcher(Player bindingPlayer, EntityType entityType)
    {
        super(bindingPlayer, entityType);
    }

    @Override
    protected void initRegistry()
    {
        super.initRegistry();

        register(ValueIndex.GUARDIAN);
    }
}
