package xyz.nifeather.morph.backends.server.renderer.network.datawatcher.watchers.types;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.destroystokyo.paper.ClientOption;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoRemovePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.world.level.GameType;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Pose;
import org.joml.Vector3i;
import xyz.nifeather.morph.backends.server.renderer.network.DisplayParameters;
import xyz.nifeather.morph.backends.server.renderer.network.datawatcher.watchers.SingleWatcher;
import xyz.nifeather.morph.backends.server.renderer.network.registries.CustomEntries;
import xyz.nifeather.morph.backends.server.renderer.network.registries.CustomEntry;
import xyz.nifeather.morph.backends.server.renderer.network.registries.ValueIndex;
import xyz.nifeather.morph.misc.AnimationNames;
import xyz.nifeather.morph.misc.NmsRecord;

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

                var packetRemove = PacketContainer.fromPacket(new ClientboundRemoveEntitiesPacket(player.getEntityId()));
                var protocol = ProtocolLibrary.getProtocolManager();

                affected.forEach(p ->
                {
                    protocol.sendServerPacket(p, packetRemove);

                    spawnPackets.forEach(packet -> protocol.sendServerPacket(p, packet));
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
                    this.writePersistent(ValueIndex.PLAYER.POSE, Pose.SLEEPING);

                    var playerPos = getBindingPlayer().getLocation();
                    var vec3i = new Vector3i(playerPos.getBlockX(), playerPos.getBlockY(), playerPos.getBlockZ());
                    this.writePersistent(ValueIndex.PLAYER.BED_POS, Optional.of(vec3i));
                }
                case AnimationNames.CRAWL ->
                {
                    resetValues();
                    this.writePersistent(ValueIndex.PLAYER.POSE, Pose.SWIMMING);
                }
                case AnimationNames.STANDUP, AnimationNames.RESET ->
                {
                    this.writePersistent(ValueIndex.PLAYER.POSE, getBindingPlayer().getPose());
                    resetValues();
                }
            }
        }
    }

    public List<Packet<?>> buildPlayerInfoPackets()
    {
        var spawnUUID = this.readEntryOrThrow(CustomEntries.SPAWN_UUID);
        var infoRemove = new ClientboundPlayerInfoRemovePacket(List.of(spawnUUID));

        var infoUpdate = new ClientboundPlayerInfoUpdatePacket(
                EnumSet.of(
                        ClientboundPlayerInfoUpdatePacket.Action.ADD_PLAYER,
                        ClientboundPlayerInfoUpdatePacket.Action.UPDATE_LISTED
                ),
                new ClientboundPlayerInfoUpdatePacket.Entry(
                        spawnUUID, this.readEntryOrThrow(CustomEntries.PROFILE),
                        this.readEntryOrDefault(CustomEntries.PROFILE_LISTED, false),
                        114514, GameType.DEFAULT_MODE,
                        null, true, 999, null
                )
        );

        return List.of(infoRemove, infoUpdate);
    }

    @Override
    public List<PacketContainer> buildSpawnPackets()
    {
        var list = new ObjectArrayList<PacketContainer>();

        var gameProfile = this.readEntryOrThrow(CustomEntries.PROFILE);

        if (gameProfile.getName().isBlank())
            throw new IllegalArgumentException("GameProfile name is empty!");

        this.buildPlayerInfoPackets().forEach(nmsPacket -> list.add(PacketContainer.fromPacket(nmsPacket)));

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
