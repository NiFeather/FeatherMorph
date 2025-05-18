package xyz.nifeather.morph.backends.server.renderer.network.datawatcher.watchers.types;

import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityEquipment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import xyz.nifeather.morph.backends.server.renderer.network.ProtocolEquipment;
import xyz.nifeather.morph.backends.server.renderer.network.registries.CustomEntries;
import xyz.nifeather.morph.backends.server.renderer.network.registries.CustomEntry;
import xyz.nifeather.morph.misc.DisguiseEquipment;

public class InventoryLivingWatcher extends LivingEntityWatcher
{
    public InventoryLivingWatcher(Player bindingPlayer, EntityType entityType)
    {
        super(bindingPlayer, entityType);
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
