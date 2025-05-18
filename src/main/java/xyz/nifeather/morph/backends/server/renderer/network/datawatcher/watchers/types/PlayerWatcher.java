package xyz.nifeather.morph.backends.server.renderer.network.datawatcher.watchers.types;

import com.destroystokyo.paper.ClientOption;
import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.entity.pose.EntityPose;
import com.github.retrooper.packetevents.protocol.player.GameMode;
import com.github.retrooper.packetevents.util.Vector3i;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerDestroyEntities;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerInfoRemove;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerInfoUpdate;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import xyz.nifeather.morph.backends.server.renderer.network.registries.CustomEntries;
import xyz.nifeather.morph.backends.server.renderer.network.registries.CustomEntry;
import xyz.nifeather.morph.backends.server.renderer.network.registries.ValueIndex;
import xyz.nifeather.morph.misc.AnimationNames;
import xyz.nifeather.morph.misc.MorphGameProfile;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

public class PlayerWatcher extends InventoryLivingWatcher
{
    @Override
    protected void initRegistry()
    {
        super.initRegistry();

        register(ValueIndex.PLAYER);
    }

    public PlayerWatcher(Player bindingPlayer)
    {
        super(bindingPlayer, EntityType.PLAYER);
    }

    @Override
    protected void doSync()
    {
        super.doSync();

        var bindingPlayer = getBindingPlayer();
        this.writeTemp(ValueIndex.PLAYER.SKIN_FLAGS, (byte)bindingPlayer.getClientOption(ClientOption.SKIN_PARTS).getRaw());
        this.writeTemp(ValueIndex.PLAYER.MAINHAND, (byte)bindingPlayer.getMainHand().ordinal());
    }

    @Override
    protected <X> void onEntryWrite(CustomEntry<X> entry, X oldVal, X newVal)
    {
        super.onEntryWrite(entry, oldVal, newVal);

        if (entry.equals(CustomEntries.PROFILE) && isPlayerOnline() && !isSilent())
        {
            var player = getBindingPlayer();
            var affected = getAffectedPlayers(player);

            if (!affected.isEmpty())
            {
                var spawnPackets = this.buildSpawnPackets();

                var protocol = PacketEvents.getAPI().getPlayerManager();

                affected.forEach(p ->
                {
                    spawnPackets.forEach(packet -> protocol.sendPacket(p, packet));
                });
            }
        }

        if (entry.equals(CustomEntries.ANIMATION))
        {
            var animId = newVal + "";

            switch (animId)
            {
                case AnimationNames.LAY ->
                {
                    this.remove(ValueIndex.PLAYER.POSE);
                    this.writePersistent(ValueIndex.PLAYER.POSE, EntityPose.SLEEPING);

                    var playerPos = getBindingPlayer().getLocation();
                    var vec3i = new Vector3i(playerPos.getBlockX(), playerPos.getBlockY(), playerPos.getBlockZ());
                    this.writePersistent(ValueIndex.PLAYER.BED_POS, Optional.of(vec3i));
                }
                case AnimationNames.CRAWL ->
                {
                    resetValues();
                    this.writePersistent(ValueIndex.PLAYER.POSE, EntityPose.SWIMMING);
                }
                case AnimationNames.STANDUP, AnimationNames.RESET ->
                {
                    this.writePersistent(ValueIndex.PLAYER.POSE, SpigotConversionUtil.fromBukkitPose(getBindingPlayer().getPose()));
                    resetValues();
                }
            }
        }
    }

    public List<PacketWrapper<?>> buildPlayerInfoPackets()
    {
        var spawnUUID = this.readEntryOrThrow(CustomEntries.SPAWN_UUID);
        var infoRemove = new WrapperPlayServerPlayerInfoRemove(List.of(spawnUUID));

        var packetProfile = MorphGameProfile.toPacketEventsUserProfile(this.readEntryOrThrow(CustomEntries.PROFILE));
        packetProfile.setUUID(spawnUUID);
        var infoUpdate = new WrapperPlayServerPlayerInfoUpdate(
                EnumSet.of(
                        WrapperPlayServerPlayerInfoUpdate.Action.ADD_PLAYER,
                        WrapperPlayServerPlayerInfoUpdate.Action.UPDATE_LISTED
                ),
                new WrapperPlayServerPlayerInfoUpdate.PlayerInfo(
                        packetProfile,
                        this.readEntryOrDefault(CustomEntries.PROFILE_LISTED, false), 114514, GameMode.defaultGameMode(), null,
                        null, 0, true
                )
        );

        return List.of(infoRemove, infoUpdate);
    }

    @Override
    public List<PacketWrapper<?>> buildSpawnPackets()
    {
        var list = new ObjectArrayList<PacketWrapper<?>>();

        var gameProfile = this.readEntryOrThrow(CustomEntries.PROFILE);

        if (gameProfile.getName().isBlank())
            throw new IllegalArgumentException("GameProfile name is empty!");

        list.addAll(this.buildPlayerInfoPackets());
        list.addAll(super.buildSpawnPackets());

        return list;
    }

    private void resetValues()
    {
        this.remove(ValueIndex.PLAYER.POSE);
        this.writePersistent(ValueIndex.PLAYER.BED_POS, Optional.empty());
        this.remove(ValueIndex.PLAYER.BED_POS);
    }
}
