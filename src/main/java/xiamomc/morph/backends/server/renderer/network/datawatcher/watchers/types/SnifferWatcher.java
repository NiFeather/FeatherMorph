package xiamomc.morph.backends.server.renderer.network.datawatcher.watchers.types;

import net.minecraft.world.entity.animal.sniffer.Sniffer;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import xiamomc.morph.backends.server.renderer.network.registries.EntryIndex;
import xiamomc.morph.backends.server.renderer.network.registries.RegistryKey;
import xiamomc.morph.backends.server.renderer.network.registries.ValueIndex;
import xiamomc.morph.misc.animation.AnimationNames;

public class SnifferWatcher extends LivingEntityWatcher
{
    public SnifferWatcher(Player bindingPlayer)
    {
        super(bindingPlayer, EntityType.SNIFFER);
    }

    @Override
    protected void initRegistry()
    {
        super.initRegistry();

        register(ValueIndex.SNIFFER);
    }

    @Override
    protected <X> void onCustomWrite(RegistryKey<X> key, X oldVal, X newVal)
    {
        super.onCustomWrite(key, oldVal, newVal);

        if (key.equals(EntryIndex.ANIMATION))
        {
            var bindingPlayer = getBindingPlayer();
            var world = bindingPlayer.getWorld();
            var id = newVal.toString();

            switch (id)
            {
                case AnimationNames.SNIFF ->
                {
                    this.writeOverride(ValueIndex.SNIFFER.SNIFFER_STATE, Sniffer.State.SNIFFING);
                    world.playSound(bindingPlayer.getLocation(), Sound.ENTITY_SNIFFER_SNIFFING, SoundCategory.NEUTRAL, 1, 1);
                }
                case AnimationNames.RESET ->
                {
                    this.writeOverride(ValueIndex.SNIFFER.SNIFFER_STATE, Sniffer.State.IDLING);
                    this.remove(ValueIndex.SNIFFER.SNIFFER_STATE);
                }
                default -> logger.warn("Unknown animation sequence id '%s'".formatted(id));
            }
        }
    }
}
