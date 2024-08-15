package xiamomc.morph.backends.server.renderer.network.datawatcher.watchers.types;

import com.comphenix.protocol.events.PacketContainer;
import net.minecraft.network.protocol.game.ClientboundEntityEventPacket;
import net.minecraft.world.entity.Pose;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import xiamomc.morph.backends.server.renderer.network.registries.EntryIndex;
import xiamomc.morph.backends.server.renderer.network.registries.RegistryKey;
import xiamomc.morph.backends.server.renderer.network.registries.ValueIndex;
import xiamomc.morph.misc.animation.AnimationNames;

public class WardenWatcher extends EHasAttackAnimationWatcher
{
    public WardenWatcher(Player bindingPlayer)
    {
        super(bindingPlayer, EntityType.WARDEN);
    }

    @Override
    protected <X> void onEntryWrite(RegistryKey<X> key, X oldVal, X newVal)
    {
        super.onEntryWrite(key, oldVal, newVal);

        var bindingPlayer = getBindingPlayer();

        if (key.equals(EntryIndex.WARDEN_CHARGING_ATTACK) && Boolean.TRUE.equals(newVal))
        {
            var entity = ((CraftPlayer)bindingPlayer).getHandle();
            sendPacketToAffectedPlayers(PacketContainer.fromPacket(new ClientboundEntityEventPacket(entity, (byte)62)));
        }

        if (key.equals(EntryIndex.ANIMATION))
        {
            var id = newVal.toString();
            var world = bindingPlayer.getWorld();

            switch (id)
            {
                case AnimationNames.ROAR -> this.writePersistent(ValueIndex.BASE_LIVING.POSE, Pose.ROARING);
                case AnimationNames.ROAR_SOUND -> world.playSound(bindingPlayer.getLocation(), Sound.ENTITY_WARDEN_ROAR, SoundCategory.HOSTILE, 3, 1);
                case AnimationNames.SNIFF ->
                {
                    this.writePersistent(ValueIndex.BASE_LIVING.POSE, Pose.SNIFFING);

                    world.playSound(bindingPlayer.getLocation(), Sound.ENTITY_WARDEN_SNIFF, SoundCategory.HOSTILE, 5, 1);
                }
                case AnimationNames.RESET -> this.remove(ValueIndex.BASE_LIVING.POSE);
                case AnimationNames.DISAPPEAR -> this.writePersistent(ValueIndex.BASE_LIVING.POSE, Pose.DIGGING);
                case AnimationNames.APPEAR -> this.writePersistent(ValueIndex.BASE_LIVING.POSE, Pose.EMERGING);
                default -> logger.warn("Unknown animation sequence id '%s'".formatted(id));
            }
        }
    }
}
