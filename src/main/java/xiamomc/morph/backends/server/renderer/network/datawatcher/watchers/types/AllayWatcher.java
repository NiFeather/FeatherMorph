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
    protected <X> void onCustomWrite(RegistryKey<X> key, X oldVal, X newVal)
    {
        super.onCustomWrite(key, oldVal, newVal);

        if (key.equals(EntryIndex.ANIMATION))
        {
            var id = newVal.toString();

            switch (id)
            {
                case AnimationNames.CLIENT_DANCE_START -> write(ValueIndex.ALLAY.DANCING, true);
                case AnimationNames.CLIENT_DANCE_STOP -> write(ValueIndex.ALLAY.DANCING, false);
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
