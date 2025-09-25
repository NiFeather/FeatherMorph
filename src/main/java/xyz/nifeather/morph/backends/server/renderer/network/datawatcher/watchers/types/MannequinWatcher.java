package xyz.nifeather.morph.backends.server.renderer.network.datawatcher.watchers.types;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import xyz.nifeather.morph.backends.server.renderer.network.registries.ValueIndex;

public class MannequinWatcher extends LivingEntityWatcher
{
    public MannequinWatcher(Player bindingPlayer)
    {
        super(bindingPlayer, EntityType.MANNEQUIN);
    }

    @Override
    protected void initRegistry()
    {
        super.initRegistry();

        register(ValueIndex.MANNEQUIN);
    }
}
