package xyz.nifeather.morph.backends.server.renderer.network.datawatcher.watchers.types;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import xiamomc.pluginbase.Annotations.Resolved;
import xyz.nifeather.morph.backends.server.renderer.network.PacketFactory;
import xyz.nifeather.morph.backends.server.renderer.network.registries.CustomEntries;
import xyz.nifeather.morph.backends.server.renderer.network.registries.CustomEntry;

public class InventoryLivingWatcher extends LivingEntityWatcher
{
    public InventoryLivingWatcher(Player bindingPlayer, EntityType entityType)
    {
        super(bindingPlayer, entityType);
    }

    @Resolved(shouldSolveImmediately = true)
    private PacketFactory packetFactory;

    @Override
    protected <X> void onEntryWrite(CustomEntry<X> entry, X oldVal, X newVal)
    {
        super.onEntryWrite(entry, oldVal, newVal);

        if (entry.equals(CustomEntries.DISPLAY_FAKE_EQUIPMENT) || entry.equals(CustomEntries.EQUIPMENT))
            sendPacketToAffectedPlayers(packetFactory.getEquipmentPacket(getBindingPlayer(), this));
    }
}
