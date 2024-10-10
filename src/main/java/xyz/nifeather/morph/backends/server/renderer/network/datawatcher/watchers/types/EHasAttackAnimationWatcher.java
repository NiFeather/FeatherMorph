package xyz.nifeather.morph.backends.server.renderer.network.datawatcher.watchers.types;

import com.comphenix.protocol.events.PacketContainer;
import net.minecraft.network.protocol.game.ClientboundEntityEventPacket;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import xyz.nifeather.morph.backends.server.renderer.network.registries.CustomEntries;
import xyz.nifeather.morph.backends.server.renderer.network.registries.CustomEntry;

public class EHasAttackAnimationWatcher extends LivingEntityWatcher
{
    public EHasAttackAnimationWatcher(Player bindingPlayer, EntityType entityType)
    {
        super(bindingPlayer, entityType);
    }

    @Override
    protected <X> void onEntryWrite(CustomEntry<X> entry, X oldVal, X newVal)
    {
        super.onEntryWrite(entry, oldVal, newVal);

        if (entry.equals(CustomEntries.ATTACK_ANIMATION) && Boolean.TRUE.equals(newVal))
        {
            var entity = ((CraftPlayer)getBindingPlayer()).getHandle();
            sendPacketToAffectedPlayers(PacketContainer.fromPacket(new ClientboundEntityEventPacket(entity, (byte)4)));
        }
    }
}
