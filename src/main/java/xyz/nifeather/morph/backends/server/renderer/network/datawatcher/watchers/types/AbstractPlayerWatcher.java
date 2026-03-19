package xyz.nifeather.morph.backends.server.renderer.network.datawatcher.watchers.types;

import com.destroystokyo.paper.ClientOption;
import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.player.GameMode;
import com.github.retrooper.packetevents.protocol.player.HumanoidArm;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import com.github.retrooper.packetevents.wrapper.play.server.*;
import com.mojang.authlib.GameProfile;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.MainHand;
import xyz.nifeather.morph.backends.server.renderer.network.registries.CustomEntries;
import xyz.nifeather.morph.backends.server.renderer.network.registries.CustomEntry;
import xyz.nifeather.morph.backends.server.renderer.network.registries.ValueIndex;
import xyz.nifeather.morph.misc.BuildFailedException;
import xyz.nifeather.morph.misc.disguiseProperty.DisguiseProperties;
import xyz.nifeather.morph.misc.disguiseProperty.SingleProperty;
import xyz.nifeather.morph.misc.disguiseProperty.values.PlayerPropertyCollection;
import xyz.nifeather.morph.utilities.GameProfileUtils;

import java.util.EnumSet;
import java.util.List;

public abstract class AbstractPlayerWatcher extends LivingEntityWatcher
{
    @Override
    protected void initRegistry()
    {
        super.initRegistry();

        register(ValueIndex.PLAYER);
    }

    private final PlayerPropertyCollection playerDisguiseProperties;

    public AbstractPlayerWatcher(Player bindingPlayer)
    {
        super(bindingPlayer, EntityType.PLAYER);

        this.playerDisguiseProperties = DisguiseProperties.INSTANCE.getCollectionOrThrow(PlayerPropertyCollection.class);
    }

    @Override
    protected void doSync()
    {
        super.doSync();

        var bindingPlayer = getBindingPlayer();
        this.writeTemp(ValueIndex.PLAYER.SKIN_FLAGS, (byte)bindingPlayer.getClientOption(ClientOption.SKIN_PARTS).getRaw());

        if (!this.isValuePresent(ValueIndex.PLAYER.MAINHAND))
            this.writeTemp(ValueIndex.PLAYER.MAINHAND, bindingPlayer.getMainHand() == MainHand.LEFT ? HumanoidArm.LEFT : HumanoidArm.RIGHT);
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

        if (entry.equals(CustomEntries.PROFILE) && isPlayerOnline() && !isSilent())
        {
            var player = getBindingPlayer();
            var affected = getAffectedPlayers(player);

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

    public List<PacketWrapper<?>> buildSpawnPackets(boolean includePlayerInfo) throws BuildFailedException
    {
        var list = new ObjectArrayList<PacketWrapper<?>>();

        var gameProfile = this.readEntryOrThrow(CustomEntries.PROFILE);

        if (gameProfile.name().isBlank())
            throw new IllegalArgumentException("GameProfile name is empty!");

        if (includePlayerInfo)
            list.addAll(this.buildPlayerInfoPackets());

        list.addAll(super.buildSpawnPackets());

        return list;
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
    public List<PacketWrapper<?>> buildSpawnPackets() throws BuildFailedException
    {
        return buildSpawnPackets(true);
    }

    @Override
    public void onEntityDestroy(Player packetReceiver)
    {
        super.onEntityDestroy(packetReceiver);

        var packet = new WrapperPlayServerPlayerInfoRemove(this.readEntryOrThrow(CustomEntries.SPAWN_UUID));
        var protocol = PacketEvents.getAPI().getPlayerManager();
        protocol.sendPacket(packetReceiver, packet);
    }

    @Override
    public boolean haveAnimation(WrapperPlayServerEntityAnimation.EntityAnimationType animationType)
    {
        return true;
    }
}
