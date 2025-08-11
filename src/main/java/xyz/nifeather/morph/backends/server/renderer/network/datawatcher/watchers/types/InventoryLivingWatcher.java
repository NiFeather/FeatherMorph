package xyz.nifeather.morph.backends.server.renderer.network.datawatcher.watchers.types;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import xyz.nifeather.morph.backends.server.renderer.network.registries.CustomEntries;
import xyz.nifeather.morph.backends.server.renderer.network.registries.CustomEntry;
import xyz.nifeather.morph.misc.DisguiseEquipment;
import xyz.nifeather.morph.misc.disguiseProperty.SingleProperty;
import xyz.nifeather.morph.misc.disguiseProperty.values.OffTreeProperties;

public class InventoryLivingWatcher extends LivingEntityWatcher
{
    public InventoryLivingWatcher(Player bindingPlayer, EntityType entityType)
    {
        super(bindingPlayer, entityType);
    }

    @Override
    protected <X> void onPropertyWrite(SingleProperty<X> property, X value)
    {
        super.onPropertyWrite(property, value);

        if (property.equals(OffTreeProperties.FAKE_EQUIPMENT))
            this.writeEntry(CustomEntries.EQUIPMENT, (DisguiseEquipment) value);
        else if (property.equals(OffTreeProperties.DISPLAY_FAKE_EQUIPMENT))
            this.writeEntry(CustomEntries.DISPLAY_FAKE_EQUIPMENT, Boolean.TRUE.equals(value));
    }

    @Override
    protected <X> void onEntryWrite(CustomEntry<X> entry, X oldVal, X newVal)
    {
        super.onEntryWrite(entry, oldVal, newVal);

        if (entry.equals(CustomEntries.DISPLAY_FAKE_EQUIPMENT) || entry.equals(CustomEntries.EQUIPMENT))
        {
            if (!isSilent())
                sendPacketToAffectedPlayers(this.getEquipmentPacket());
        }
    }
}
