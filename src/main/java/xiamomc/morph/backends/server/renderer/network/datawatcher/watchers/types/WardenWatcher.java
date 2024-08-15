package xiamomc.morph.backends.server.renderer.network.datawatcher.watchers.types;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import net.minecraft.network.protocol.game.ClientboundEntityEventPacket;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.world.entity.Pose;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import xiamomc.morph.backends.server.renderer.network.DisplayParameters;
import xiamomc.morph.backends.server.renderer.network.registries.CustomEntries;
import xiamomc.morph.backends.server.renderer.network.registries.RegistryKey;
import xiamomc.morph.backends.server.renderer.network.registries.ValueIndex;
import xiamomc.morph.misc.NmsRecord;
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

        if (key.equals(CustomEntries.WARDEN_CHARGING_ATTACK) && Boolean.TRUE.equals(newVal))
        {
            var entity = ((CraftPlayer)bindingPlayer).getHandle();
            sendPacketToAffectedPlayers(PacketContainer.fromPacket(new ClientboundEntityEventPacket(entity, (byte)62)));
        }

        if (key.equals(CustomEntries.ANIMATION))
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
                case AnimationNames.RESET ->
                {
                    this.writePersistent(ValueIndex.BASE_ENTITY.GENERAL, this.getPlayerBitMask(bindingPlayer));
                    this.writePersistent(ValueIndex.BASE_LIVING.POSE, NmsRecord.ofPlayer(bindingPlayer).getPose());
                    this.remove(ValueIndex.BASE_LIVING.POSE);
                    this.remove(ValueIndex.BASE_ENTITY.GENERAL);
                }
                case AnimationNames.DIGDOWN ->
                {
                    if (this.read(ValueIndex.BASE_LIVING.POSE) == Pose.DIGGING) return;

                    this.writePersistent(ValueIndex.BASE_LIVING.POSE, Pose.DIGGING);
                    world.playSound(bindingPlayer.getLocation(), Sound.ENTITY_WARDEN_DIG, 5, 1);
                }
                case AnimationNames.VANISH -> this.writePersistent(ValueIndex.BASE_ENTITY.GENERAL, (byte)0x20);
                case AnimationNames.APPEAR ->
                {
                    if (this.read(ValueIndex.BASE_LIVING.POSE) != Pose.DIGGING) return;

                    this.remove(ValueIndex.BASE_ENTITY.GENERAL);
                    this.writePersistent(ValueIndex.BASE_LIVING.POSE, Pose.EMERGING);
                    world.playSound(bindingPlayer.getLocation(), Sound.ENTITY_WARDEN_EMERGE, 5, 1);

                    var parameters = new DisplayParameters(this.getEntityType(), this, null);
                    var packets = getPacketFactory().buildSpawnPackets(bindingPlayer, parameters);
                    var affectedPlayers = this.getAffectedPlayers(bindingPlayer);
                    var protocol = ProtocolLibrary.getProtocolManager();
                    var despawnPacket = PacketContainer.fromPacket(new ClientboundRemoveEntitiesPacket(bindingPlayer.getEntityId()));

                    for (Player affectedPlayer : affectedPlayers)
                    {
                        protocol.sendServerPacket(affectedPlayer, despawnPacket);
                        packets.forEach(p -> protocol.sendServerPacket(affectedPlayer, p));
                    }
                }
            }
        }
    }
}
