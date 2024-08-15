package xiamomc.morph.backends.server.renderer.network.datawatcher.watchers.types;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import xiamomc.morph.backends.server.renderer.network.registries.EntryIndex;
import xiamomc.morph.backends.server.renderer.network.registries.RegistryKey;
import xiamomc.morph.backends.server.renderer.network.registries.ValueIndex;
import xiamomc.morph.misc.animation.AnimationNames;

public class AllayWatcher extends LivingEntityWatcher
{
    @Override
    protected void initRegistry()
    {
        super.initRegistry();

        register(ValueIndex.ALLAY);
    }

    @Override
    protected <X> void onEntryWrite(RegistryKey<X> key, X oldVal, X newVal)
    {
        super.onEntryWrite(key, oldVal, newVal);

        if (key.equals(EntryIndex.ANIMATION))
        {
            var id = newVal.toString();

            switch (id)
            {
                case AnimationNames.DANCE_START -> writePersistent(ValueIndex.ALLAY.DANCING, true);
                case AnimationNames.STOP, AnimationNames.RESET -> writePersistent(ValueIndex.ALLAY.DANCING, false);
            }
        }
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
