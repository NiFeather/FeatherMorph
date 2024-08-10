package xiamomc.morph.backends.server.renderer.network.datawatcher.watchers.types;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.destroystokyo.paper.ClientOption;
import com.mojang.authlib.GameProfile;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.world.entity.Pose;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import xiamomc.morph.backends.server.renderer.network.DisplayParameters;
import xiamomc.morph.backends.server.renderer.network.registries.EntryIndex;
import xiamomc.morph.backends.server.renderer.network.registries.RegistryKey;
import xiamomc.morph.backends.server.renderer.network.registries.ValueIndex;
import xiamomc.morph.misc.NmsRecord;
import xiamomc.morph.misc.animation.AnimationNames;

import java.util.UUID;

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
        this.write(ValueIndex.PLAYER.SKIN_FLAGS, (byte)bindingPlayer.getClientOption(ClientOption.SKIN_PARTS).getRaw());
        this.write(ValueIndex.PLAYER.MAINHAND, (byte)bindingPlayer.getMainHand().ordinal());
    }

    @Override
    protected <X> void onCustomWrite(RegistryKey<X> key, X oldVal, X newVal)
    {
        super.onCustomWrite(key, oldVal, newVal);

        if (key.equals(EntryIndex.PROFILE) && isPlayerOnline())
        {
            var player = getBindingPlayer();

            var profile = newVal == null
                    ? new GameProfile(UUID.randomUUID(), this.getOrDefault(EntryIndex.DISGUISE_NAME, ""))
                    : (GameProfile) newVal;

            var spawnPackets = getPacketFactory()
                    .buildSpawnPackets(player,
                            new DisplayParameters(this.getEntityType(), this, profile));

            var packetRemove = PacketContainer.fromPacket(new ClientboundRemoveEntitiesPacket(player.getEntityId()));
            var protocol = ProtocolLibrary.getProtocolManager();

            var affected = getAffectedPlayers(player);
            affected.forEach(p ->
            {
                protocol.sendServerPacket(p, packetRemove);

                spawnPackets.forEach(packet -> protocol.sendServerPacket(p, packet));
            });
        }

        if (key.equals(EntryIndex.ANIMATION))
        {
            var animId = newVal + "";

            switch (animId)
            {
                case AnimationNames.LAY ->
                {
                    this.remove(ValueIndex.PLAYER.POSE);
                    this.write(ValueIndex.PLAYER.POSE, Pose.SLEEPING, true);
                }
                case AnimationNames.PROSTRATE ->
                {
                    this.remove(ValueIndex.PLAYER.POSE);
                    this.write(ValueIndex.PLAYER.POSE, Pose.SWIMMING, true);
                }
                case AnimationNames.STANDUP ->
                {
                    var nmsPlayer = NmsRecord.ofPlayer(getBindingPlayer());
                    this.write(ValueIndex.PLAYER.POSE, nmsPlayer.getPose(), true);
                    this.remove(ValueIndex.PLAYER.POSE);
                }
            }
        }
    }
}
