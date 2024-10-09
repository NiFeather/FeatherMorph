package xyz.nifeather.morph.backends.server.renderer.network.datawatcher.watchers.types;

import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import xyz.nifeather.morph.backends.server.renderer.network.registries.CustomEntries;
import xyz.nifeather.morph.backends.server.renderer.network.registries.RegistryKey;
import xyz.nifeather.morph.backends.server.renderer.network.registries.ValueIndex;
import xyz.nifeather.morph.misc.AnimationNames;

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
    protected <X> void onEntryWrite(RegistryKey<X> key, X oldVal, X newVal)
    {
        super.onEntryWrite(key, oldVal, newVal);

        if (key.equals(CustomEntries.ANIMATION))
        {
            var animId = newVal.toString();

            var world = this.getBindingPlayer().getWorld();
            switch (animId)
            {
                case AnimationNames.PEEK_START ->
                {
                    this.writePersistent(ValueIndex.SHULKER.PEEK_ID, (byte)30);
                    world.playSound(getBindingPlayer().getLocation(), Sound.ENTITY_SHULKER_OPEN, SoundCategory.HOSTILE, 1, 1);
                }

                case AnimationNames.OPEN_START ->
                {
                    this.writePersistent(ValueIndex.SHULKER.PEEK_ID, (byte)100);
                    world.playSound(getBindingPlayer().getLocation(), Sound.ENTITY_SHULKER_OPEN, SoundCategory.HOSTILE, 1, 1);
                }

                case AnimationNames.PEEK_STOP, AnimationNames.OPEN_STOP ->
                {
                    this.writePersistent(ValueIndex.SHULKER.PEEK_ID, (byte)0);
                    world.playSound(getBindingPlayer().getLocation(), Sound.ENTITY_SHULKER_CLOSE, SoundCategory.HOSTILE, 1, 1);
                }

                case AnimationNames.RESET ->
                {
                    this.writePersistent(ValueIndex.SHULKER.PEEK_ID, (byte)0);
                    this.remove(ValueIndex.SHULKER.PEEK_ID);
                }
            }
        }
    }
}
