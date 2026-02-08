package xyz.nifeather.morph.backends.server.renderer.network.datawatcher.watchers.types;

import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityStatus;
import net.minecraft.world.entity.EntityEvent;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import xyz.nifeather.morph.backends.server.renderer.network.datawatcher.syncing.IBindTarget;
import xyz.nifeather.morph.backends.server.renderer.network.registries.CustomEntries;
import xyz.nifeather.morph.backends.server.renderer.network.registries.CustomEntry;

public class EHasAttackAnimationWatcher extends LivingEntityWatcher
{
    public EHasAttackAnimationWatcher(IBindTarget bindTarget, EntityType entityType)
    {
        super(bindTarget, entityType);
    }

    @Override
    protected <X> void onEntryWrite(CustomEntry<X> entry, X oldVal, X newVal)
    {
        super.onEntryWrite(entry, oldVal, newVal);

        if (entry.equals(CustomEntries.ATTACK_ANIMATION) && Boolean.TRUE.equals(newVal))
            sendPacketToAffectedPlayers(new WrapperPlayServerEntityStatus(readEntryOrThrow(CustomEntries.SPAWN_ID), EntityEvent.START_ATTACKING));
    }
}
