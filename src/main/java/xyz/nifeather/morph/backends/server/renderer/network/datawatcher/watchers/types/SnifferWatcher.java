package xyz.nifeather.morph.backends.server.renderer.network.datawatcher.watchers.types;

import com.github.retrooper.packetevents.protocol.entity.sniffer.SnifferState;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Sniffer;
import xyz.nifeather.morph.backends.server.renderer.network.registries.CustomEntries;
import xyz.nifeather.morph.backends.server.renderer.network.registries.CustomEntry;
import xyz.nifeather.morph.backends.server.renderer.network.registries.ValueIndex;
import xyz.nifeather.morph.misc.AnimationNames;
import xyz.nifeather.morph.misc.disguiseProperty.PropertyNames;
import xyz.nifeather.morph.misc.disguiseProperty.SingleProperty;

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
    protected <X> void onPropertyWrite(SingleProperty<X> property, X value)
    {
        super.onPropertyWrite(property, value);

        if (property.id().equals(PropertyNames.SNIFFER_STATE))
        {
            var state = (Sniffer.State) value;

            SnifferState peState = switch (state)
            {
                case IDLING -> SnifferState.IDLING;
                case FEELING_HAPPY -> SnifferState.FEELING_HAPPY;
                case SCENTING -> SnifferState.SCENTING;
                case SNIFFING -> SnifferState.SNIFFING;
                case SEARCHING -> SnifferState.SEARCHING;
                case DIGGING -> SnifferState.DIGGING;
                case RISING -> SnifferState.RISING;
            };

            this.writePersistent(ValueIndex.SNIFFER.SNIFFER_STATE, peState);
        }
    }
}
