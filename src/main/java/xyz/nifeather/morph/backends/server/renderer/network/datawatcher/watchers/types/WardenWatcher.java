package xyz.nifeather.morph.backends.server.renderer.network.datawatcher.watchers.types;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.entity.pose.EntityPose;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerDestroyEntities;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityStatus;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import xyz.nifeather.morph.backends.server.renderer.network.registries.CustomEntries;
import xyz.nifeather.morph.backends.server.renderer.network.registries.CustomEntry;
import xyz.nifeather.morph.backends.server.renderer.network.registries.ValueIndex;
import xyz.nifeather.morph.misc.AnimationNames;

public class WardenWatcher extends EHasAttackAnimationWatcher
{
    public WardenWatcher(Player bindingPlayer)
    {
        super(bindingPlayer, EntityType.WARDEN);
    }

    @Override
    protected <X> void onEntryWrite(CustomEntry<X> entry, X oldVal, X newVal)
    {
        super.onEntryWrite(entry, oldVal, newVal);

        var bindingPlayer = getBindingPlayer();

        if (entry.equals(CustomEntries.WARDEN_CHARGING_ATTACK) && Boolean.TRUE.equals(newVal))
            sendPacketToAffectedPlayers(new WrapperPlayServerEntityStatus(getBindingPlayer().getEntityId(), 62));

        if (entry.equals(CustomEntries.ANIMATION))
        {
            var id = newVal.toString();
            var world = bindingPlayer.getWorld();

            switch (id)
            {
                case AnimationNames.ROAR ->
                {
                    if (this.readEntryOrDefault(CustomEntries.WARDEN_VANISHED, false)) return;

                    this.block(ValueIndex.BASE_LIVING.POSE);
                    this.writePersistent(ValueIndex.BASE_LIVING.POSE, EntityPose.ROARING);
                }
                case AnimationNames.ROAR_SOUND ->
                {
                    if (this.readEntryOrDefault(CustomEntries.WARDEN_VANISHED, false)) return;

                    world.playSound(bindingPlayer.getLocation(), Sound.ENTITY_WARDEN_ROAR, SoundCategory.HOSTILE, 3, 1);
                }
                case AnimationNames.SNIFF ->
                {
                    if (this.readEntryOrDefault(CustomEntries.WARDEN_VANISHED, false)) return;

                    this.block(ValueIndex.BASE_LIVING.POSE);
                    this.writePersistent(ValueIndex.BASE_LIVING.POSE, EntityPose.SNIFFING);

                    world.playSound(bindingPlayer.getLocation(), Sound.ENTITY_WARDEN_SNIFF, SoundCategory.HOSTILE, 5, 1);
                }
                case AnimationNames.DIGDOWN ->
                {
                    if (this.readEntryOrDefault(CustomEntries.WARDEN_VANISHED, false)) return;

                    this.block(ValueIndex.BASE_LIVING.POSE);
                    this.writePersistent(ValueIndex.BASE_LIVING.POSE, EntityPose.DIGGING);
                    world.playSound(bindingPlayer.getLocation(), Sound.ENTITY_WARDEN_DIG, 5, 1);
                }
                case AnimationNames.VANISH ->
                {
                    this.writePersistent(ValueIndex.BASE_ENTITY.GENERAL, (byte)0x20);
                    this.writePersistent(ValueIndex.BASE_LIVING.SILENT, true);
                    this.writePersistent(ValueIndex.BASE_LIVING.POSE, EntityPose.SLEEPING);
                    this.writeEntry(CustomEntries.WARDEN_VANISHED, true);
                }
                case AnimationNames.APPEAR ->
                {
                    this.writeEntry(CustomEntries.WARDEN_VANISHED, false);
                    this.block(ValueIndex.BASE_LIVING.POSE);
                    this.remove(ValueIndex.BASE_ENTITY.GENERAL);
                    this.writePersistent(ValueIndex.BASE_LIVING.POSE, EntityPose.EMERGING);
                    world.playSound(bindingPlayer.getLocation(), Sound.ENTITY_WARDEN_EMERGE, 5, 1);

                    var packets = this.buildSpawnPackets();
                    var affectedPlayers = this.getAffectedPlayers(bindingPlayer);
                    var despawnPacket = new WrapperPlayServerDestroyEntities(bindingPlayer.getEntityId());

                    var protocol = PacketEvents.getAPI().getPlayerManager();
                    for (Player affectedPlayer : affectedPlayers)
                    {
                        protocol.sendPacket(affectedPlayer, despawnPacket);
                        packets.forEach(p -> protocol.sendPacket(affectedPlayer, p));
                    }
                }
                case AnimationNames.TRY_RESET ->
                {
                    // 如果当前已消失，则不要调用重置
                    // 因为重置会将一些动作数据重新同步为玩家的数据
                    if (this.readEntryOrDefault(CustomEntries.WARDEN_VANISHED, false)) return;

                    reset();
                }
                case AnimationNames.RESET ->
                {
                    reset();
                }
            }
        }
    }

    private void reset()
    {
        var bindingPlayer = getBindingPlayer();

        this.writePersistent(ValueIndex.BASE_ENTITY.GENERAL, this.getPlayerBitMask(bindingPlayer));
        this.writePersistent(ValueIndex.BASE_LIVING.POSE, SpigotConversionUtil.fromBukkitPose(bindingPlayer.getPose()));
        this.writePersistent(ValueIndex.BASE_LIVING.SILENT, false);
        this.remove(ValueIndex.BASE_LIVING.POSE);
        this.remove(ValueIndex.BASE_ENTITY.GENERAL);
        this.remove(ValueIndex.BASE_LIVING.SILENT);

        this.unBlock(ValueIndex.BASE_LIVING.POSE);
    }
}
