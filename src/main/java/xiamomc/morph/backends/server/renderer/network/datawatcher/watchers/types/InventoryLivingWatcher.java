package xiamomc.morph.backends.server.renderer.network.datawatcher.watchers.types;

import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityEquipment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import xiamomc.morph.backends.server.renderer.network.PacketFactory;
import xiamomc.morph.backends.server.renderer.network.registries.EntryIndex;
import xiamomc.morph.backends.server.renderer.network.registries.RegistryKey;
import xiamomc.pluginbase.Annotations.Resolved;

public class InventoryLivingWatcher extends LivingEntityWatcher
{
    public InventoryLivingWatcher(Player bindingPlayer, EntityType entityType)
    {
        super(bindingPlayer, entityType);
    }

    @Resolved(shouldSolveImmediately = true)
    private PacketFactory packetFactory;

    @Override
    protected <X> void onCustomWrite(RegistryKey<X> key, X oldVal, X newVal)
    {
        super.onCustomWrite(key, oldVal, newVal);

        if (!isPlayerOnline()) return;

        if (key.equals(EntryIndex.DISPLAY_FAKE_EQUIPMENT) || key.equals(EntryIndex.EQUIPMENT))
            sendPacketToAffectedPlayers(new WrapperPlayServerEntityEquipment(getBindingPlayer().getEntityId(), packetFactory.getPacketEquipmentList(getBindingPlayer(), this)));
    }
}
