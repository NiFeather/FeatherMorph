package xiamomc.morph.backends.server.renderer.network.datawatcher.watchers.types;

import net.neoforged.art.internal.EntryImpl;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import xiamomc.morph.backends.server.renderer.network.datawatcher.values.PufferfishValues;
import xiamomc.morph.backends.server.renderer.network.registries.EntryIndex;
import xiamomc.morph.backends.server.renderer.network.registries.RegistryKey;
import xiamomc.morph.backends.server.renderer.network.registries.ValueIndex;
import xiamomc.morph.misc.animation.AnimationNames;

public class PufferfishWatcher extends LivingEntityWatcher
{
    public PufferfishWatcher(Player bindingPlayer)
    {
        super(bindingPlayer, EntityType.PUFFERFISH);
    }

    @Override
    protected <X> void onCustomWrite(RegistryKey<X> key, X oldVal, X newVal)
    {
        super.onCustomWrite(key, oldVal, newVal);

        if (key.equals(EntryIndex.ANIMATION))
        {
            var animId = newVal.toString();
            var world = getBindingPlayer().getWorld();

            var lastState = this.getOr(ValueIndex.PUFFERFISH.PUFF_STATE, 0);
            switch (animId)
            {
                case AnimationNames.INFLATE ->
                {
                    this.write(ValueIndex.PUFFERFISH.PUFF_STATE, PufferfishValues.PuffStates.LARGE);

                    if (lastState != PufferfishValues.PuffStates.LARGE)
                        world.playSound(getBindingPlayer().getLocation(), Sound.ENTITY_PUFFER_FISH_BLOW_UP, SoundCategory.HOSTILE, 1, 1);
                }
                case AnimationNames.DEFLATE ->
                {
                    this.write(ValueIndex.PUFFERFISH.PUFF_STATE, PufferfishValues.PuffStates.SMALL);

                    if (lastState != PufferfishValues.PuffStates.SMALL)
                        world.playSound(getBindingPlayer().getLocation(), Sound.ENTITY_PUFFER_FISH_BLOW_OUT, SoundCategory.HOSTILE, 1, 1);
                }
            }
        }
    }
}
