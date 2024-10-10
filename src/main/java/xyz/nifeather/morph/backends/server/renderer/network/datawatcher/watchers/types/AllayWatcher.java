package xyz.nifeather.morph.backends.server.renderer.network.datawatcher.watchers.types;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import xyz.nifeather.morph.backends.server.renderer.network.registries.CustomEntries;
import xyz.nifeather.morph.backends.server.renderer.network.registries.RegistryKey;
import xyz.nifeather.morph.backends.server.renderer.network.registries.ValueIndex;
import xyz.nifeather.morph.misc.AnimationNames;

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

        if (key.equals(CustomEntries.ANIMATION))
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