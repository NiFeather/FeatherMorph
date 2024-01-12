package xiamomc.morph.backends.server.renderer.network.datawatcher.watchers.types;

import com.comphenix.protocol.events.PacketContainer;
import net.minecraft.network.protocol.game.ClientboundEntityEventPacket;
import org.bukkit.craftbukkit.v1_20_R3.entity.CraftPlayer;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import xiamomc.morph.backends.server.renderer.network.registries.EntryIndex;
import xiamomc.morph.backends.server.renderer.network.registries.RegistryKey;

public class EHasAttackAnimationWatcher extends LivingEntityWatcher
{
    public EHasAttackAnimationWatcher(Player bindingPlayer, EntityType entityType)
    {
        super(bindingPlayer, entityType);
    }

    @Override
    protected void onCustomWrite(RegistryKey<?> key, @Nullable Object oldVal, Object newVal)
    {
        super.onCustomWrite(key, oldVal, newVal);

        if (key.equals(EntryIndex.ATTACK_ANIMATION) && Boolean.TRUE.equals(newVal))
        {
            var entity = ((CraftPlayer)getBindingPlayer()).getHandle();
            sendPacketToAffectedPlayers(PacketContainer.fromPacket(new ClientboundEntityEventPacket(entity, (byte)4)));
        }
    }
}
