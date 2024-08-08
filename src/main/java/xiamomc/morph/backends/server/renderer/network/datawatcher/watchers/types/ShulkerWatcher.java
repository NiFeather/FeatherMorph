package xiamomc.morph.backends.server.renderer.network.datawatcher.watchers.types;

import net.neoforged.art.internal.EntryImpl;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import xiamomc.morph.backends.server.renderer.network.registries.EntryIndex;
import xiamomc.morph.backends.server.renderer.network.registries.RegistryKey;
import xiamomc.morph.backends.server.renderer.network.registries.ValueIndex;
import xiamomc.morph.misc.animation.AnimationNames;

public class ShulkerWatcher extends LivingEntityWatcher
{
    public ShulkerWatcher(Player bindingPlayer)
    {
        super(bindingPlayer, EntityType.SHULKER);
    }

    @Override
    protected void initRegistry()
    {
        super.initRegistry();

        register(ValueIndex.SHULKER);
    }

    @Override
    protected <X> void onCustomWrite(RegistryKey<X> key, X oldVal, X newVal)
    {
        super.onCustomWrite(key, oldVal, newVal);

        if (key.equals(EntryIndex.ANIMATION))
        {
            var animId = newVal.toString();

            var world = this.getBindingPlayer().getWorld();
            switch (animId)
            {
                case AnimationNames.PEEK_START ->
                {
                    this.write(ValueIndex.SHULKER.PEEK_ID, (byte)30);
                    world.playSound(getBindingPlayer().getLocation(), Sound.ENTITY_SHULKER_OPEN, SoundCategory.HOSTILE, 1, 1);
                }

                case AnimationNames.OPEN_START ->
                {
                    this.write(ValueIndex.SHULKER.PEEK_ID, (byte)100);
                    world.playSound(getBindingPlayer().getLocation(), Sound.ENTITY_SHULKER_OPEN, SoundCategory.HOSTILE, 1, 1);
                }

                case AnimationNames.PEEK_STOP, AnimationNames.OPEN_STOP ->
                {
                    this.write(ValueIndex.SHULKER.PEEK_ID, (byte)0);
                    world.playSound(getBindingPlayer().getLocation(), Sound.ENTITY_SHULKER_CLOSE, SoundCategory.HOSTILE, 1, 1);
                }
            }
        }
    }
}
