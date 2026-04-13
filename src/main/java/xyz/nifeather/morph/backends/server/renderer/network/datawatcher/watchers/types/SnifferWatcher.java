package xyz.nifeather.morph.backends.server.renderer.network.datawatcher.watchers.types;

import com.github.retrooper.packetevents.protocol.entity.sniffer.SnifferState;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import xyz.nifeather.morph.backends.server.renderer.network.datawatcher.syncing.IBindTarget;
import xyz.nifeather.morph.backends.server.renderer.network.registries.CustomEntries;
import xyz.nifeather.morph.backends.server.renderer.network.registries.CustomEntry;
import xyz.nifeather.morph.backends.server.renderer.network.registries.ValueIndex;
import xyz.nifeather.morph.misc.AnimationNames;

public class SnifferWatcher extends LivingEntityWatcher
{
    public SnifferWatcher(IBindTarget bindTarget)
    {
        super(bindTarget, EntityType.SNIFFER);
    }

    @Override
    protected void initRegistry()
    {
        super.initRegistry();

        register(ValueIndex.SNIFFER);
    }

    @Override
    protected <X> void onEntryWrite(CustomEntry<X> entry, X oldVal, X newVal)
    {
        super.onEntryWrite(entry, oldVal, newVal);

        if (entry.equals(CustomEntries.ANIMATION))
        {
            var location = location();
            var world = location.getWorld();
            var id = newVal.toString();

            switch (id)
            {
                case AnimationNames.SNIFF ->
                {
                    this.writePersistent(ValueIndex.SNIFFER.SNIFFER_STATE, SnifferState.SNIFFING);
                    world.playSound(location, Sound.ENTITY_SNIFFER_SNIFFING, SoundCategory.NEUTRAL, 1, 1);
                }
                case AnimationNames.RESET ->
                {
                    this.writePersistent(ValueIndex.SNIFFER.SNIFFER_STATE, SnifferState.IDLING);
                    this.remove(ValueIndex.SNIFFER.SNIFFER_STATE);
                }
                default -> logger.warn("Unknown animation sequence id '%s'".formatted(id));
            }
        }
    }
}
