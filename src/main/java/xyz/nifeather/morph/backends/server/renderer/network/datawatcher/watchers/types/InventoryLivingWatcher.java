package xyz.nifeather.morph.backends.server.renderer.network.datawatcher.watchers.types;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import xiamomc.pluginbase.Annotations.Resolved;
import xyz.nifeather.morph.backends.server.renderer.network.PacketFactory;
import xyz.nifeather.morph.backends.server.renderer.network.registries.CustomEntries;
import xyz.nifeather.morph.backends.server.renderer.network.registries.RegistryKey;

public class InventoryLivingWatcher extends LivingEntityWatcher
{
    public InventoryLivingWatcher(Player bindingPlayer, EntityType entityType)
    {
        super(bindingPlayer, entityType);
    }

    @Resolved(shouldSolveImmediately = true)
    private PacketFactory packetFactory;

    @Override
    protected <X> void onEntryWrite(RegistryKey<X> key, X oldVal, X newVal)
    {
        super.onEntryWrite(key, oldVal, newVal);

        if (key.equals(CustomEntries.DISPLAY_FAKE_EQUIPMENT) || key.equals(CustomEntries.EQUIPMENT))
            sendPacketToAffectedPlayers(packetFactory.getEquipmentPacket(getBindingPlayer(), this));
    }
}
