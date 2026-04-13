package xyz.nifeather.morph.backends.server.renderer.network.datawatcher.watchers.types;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.entity.pose.EntityPose;
import com.github.retrooper.packetevents.protocol.player.GameMode;
import com.github.retrooper.packetevents.protocol.player.HumanoidArm;
import com.github.retrooper.packetevents.util.Vector3i;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerInfoRemove;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerInfoUpdate;
import com.mojang.authlib.GameProfile;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.MainHand;
import xyz.nifeather.morph.backends.server.renderer.network.datawatcher.syncing.IBindTarget;
import xyz.nifeather.morph.backends.server.renderer.network.registries.CustomEntries;
import xyz.nifeather.morph.backends.server.renderer.network.registries.CustomEntry;
import xyz.nifeather.morph.backends.server.renderer.network.registries.ValueIndex;
import xyz.nifeather.morph.misc.AnimationNames;
import xyz.nifeather.morph.misc.BuildFailedException;
import xyz.nifeather.morph.misc.disguiseProperty.DisguiseProperties;
import xyz.nifeather.morph.misc.disguiseProperty.SingleProperty;
import xyz.nifeather.morph.misc.disguiseProperty.values.PlayerPropertyCollection;
import xyz.nifeather.morph.utilities.GameProfileUtils;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

public class PlayerWatcher extends LivingEntityWatcher
{
    @Override
    protected void initRegistry()
    {
        super.initRegistry();

        register(ValueIndex.PLAYER);
    }

    private final PlayerPropertyCollection playerDisguiseProperties;

    public PlayerWatcher(IBindTarget bindTarget)
    {
        super(bindTarget, EntityType.PLAYER);

        this.playerDisguiseProperties = DisguiseProperties.INSTANCE.getCollectionOrThrow(PlayerPropertyCollection.class);
    }

    @Override
    protected void doSync()
    {
        super.doSync();

        this.writeTemp(ValueIndex.PLAYER.SKIN_FLAGS, bindTarget.skinFlags());

        if (!this.isValuePresent(ValueIndex.PLAYER.MAINHAND))
        {
            HumanoidArm mainHand = bindTarget.mainHand() == MainHand.LEFT ? HumanoidArm.LEFT : HumanoidArm.RIGHT;
            this.writeTemp(ValueIndex.PLAYER.MAINHAND, mainHand);
        }
    }

    @Override
    protected <X> void onPropertyWrite(SingleProperty<X> property, X value)
    {
        if (property.equals(playerDisguiseProperties.MAIN_HAND))
        {
            var handStatus = (PlayerPropertyCollection.MainHandStatus) value;
            if (handStatus == PlayerPropertyCollection.MainHandStatus.NOTSET)
                return;

            var hand = handStatus.bindingHand;
            assert hand != null;

            this.writePersistent(ValueIndex.PLAYER.MAINHAND, hand == MainHand.LEFT ? HumanoidArm.LEFT : HumanoidArm.RIGHT);
        }
        else if (property.equals(playerDisguiseProperties.SKIN))
        {
            var skin = (GameProfile) value;
            this.writeEntry(CustomEntries.PROFILE, skin);
        }

        super.onPropertyWrite(property, value);
    }

    @Override
    protected <X> void onEntryWrite(CustomEntry<X> entry, X oldVal, X newVal)
    {
        super.onEntryWrite(entry, oldVal, newVal);

        if (entry.equals(CustomEntries.PROFILE) && isActive() && !isSilent())
        {
            var affected = getAffectedPlayers();

            if (affected.isEmpty())
                return;

            List<PacketWrapper<?>> spawnPackets;

            try
            {
                spawnPackets = this.buildSpawnPackets();
            }
            catch (BuildFailedException e)
            {
                logger.error("Build spawn packet FAILED for player skin update! not continuing", e);
                return;
            }

            var protocol = PacketEvents.getAPI().getPlayerManager();

            affected.forEach(p ->
                    spawnPackets.forEach(packet -> protocol.sendPacket(p, packet)));
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

                    var position = location();
                    var vec3i = new Vector3i(position.getBlockX(), position.getBlockY(), position.getBlockZ());
                    this.writePersistent(ValueIndex.PLAYER.BED_POS, Optional.of(vec3i));
                }
                case AnimationNames.CRAWL ->
                {
                    resetValues();
                    this.writePersistent(ValueIndex.PLAYER.POSE, EntityPose.SWIMMING);
                }
                case AnimationNames.STANDUP, AnimationNames.RESET ->
                {
                    this.writePersistent(ValueIndex.PLAYER.POSE, SpigotConversionUtil.fromBukkitPose(bindTarget.pose()));
                    resetValues();
                }
            }
        }
    }

    public List<PacketWrapper<?>> buildPlayerInfoPackets()
    {
        var virtualEntityUUID = this.readEntryOrThrow(CustomEntries.SPAWN_UUID);
        var infoRemove = new WrapperPlayServerPlayerInfoRemove(List.of(virtualEntityUUID));

        var packetProfile = GameProfileUtils.toPacketEventsUserProfile(this.readEntryOrThrow(CustomEntries.PROFILE));
        packetProfile.setUUID(virtualEntityUUID);
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
    public List<PacketWrapper<?>> buildVirtualEntityDisposalPackets() throws BuildFailedException
    {
        var list = new ObjectArrayList<>(super.buildVirtualEntityDisposalPackets());

        var playerInfoRmPacket = new WrapperPlayServerPlayerInfoRemove(this.readEntryOrThrow(CustomEntries.SPAWN_UUID));
        list.add(playerInfoRmPacket);

        return list;
    }

    @Override
    protected List<PacketWrapper<?>> doBuildSpawnPackets() throws BuildFailedException
    {
        var list = new ObjectArrayList<PacketWrapper<?>>();

        var gameProfile = this.readEntryOrThrow(CustomEntries.PROFILE);

        if (gameProfile.name().isBlank())
            throw new IllegalArgumentException("GameProfile name is empty!");

        list.addAll(this.buildPlayerInfoPackets());

        list.addAll(super.doBuildSpawnPackets());

        return list;
    }

    private void resetValues()
    {
        this.remove(ValueIndex.PLAYER.POSE);
        this.writePersistent(ValueIndex.PLAYER.BED_POS, Optional.empty());
        this.remove(ValueIndex.PLAYER.BED_POS);
    }
}
