package xyz.nifeather.morph.backends.server.renderer.network.datawatcher.watchers.types;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.destroystokyo.paper.ClientOption;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.world.entity.Pose;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import xyz.nifeather.morph.backends.server.renderer.network.DisplayParameters;
import xyz.nifeather.morph.backends.server.renderer.network.registries.CustomEntries;
import xyz.nifeather.morph.backends.server.renderer.network.registries.RegistryKey;
import xyz.nifeather.morph.backends.server.renderer.network.registries.ValueIndex;
import xyz.nifeather.morph.misc.AnimationNames;
import xyz.nifeather.morph.misc.NmsRecord;

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
    protected <X> void onEntryWrite(RegistryKey<X> key, X oldVal, X newVal)
    {
        super.onEntryWrite(key, oldVal, newVal);

        if (key.equals(CustomEntries.PROFILE) && isPlayerOnline() && !isSilent())
        {
            var player = getBindingPlayer();
            var affected = getAffectedPlayers(player);

            if (!affected.isEmpty())
            {
                var spawnPackets = getPacketFactory()
                        .buildSpawnPackets(new DisplayParameters(this));

                var packetRemove = PacketContainer.fromPacket(new ClientboundRemoveEntitiesPacket(player.getEntityId()));
                var protocol = ProtocolLibrary.getProtocolManager();

                affected.forEach(p ->
                {
                    protocol.sendServerPacket(p, packetRemove);

                    spawnPackets.forEach(packet -> protocol.sendServerPacket(p, packet));
                });
            }
        }

        if (key.equals(CustomEntries.ANIMATION))
        {
            var animId = newVal + "";

            switch (animId)
            {
                case AnimationNames.LAY ->
                {
                    this.remove(ValueIndex.PLAYER.POSE);
                    this.writePersistent(ValueIndex.PLAYER.POSE, Pose.SLEEPING);
                }
                case AnimationNames.CRAWL ->
                {
                    this.remove(ValueIndex.PLAYER.POSE);
                    this.writePersistent(ValueIndex.PLAYER.POSE, Pose.SWIMMING);
                }
                case AnimationNames.STANDUP, AnimationNames.RESET ->
                {
                    var nmsPlayer = NmsRecord.ofPlayer(getBindingPlayer());
                    this.writePersistent(ValueIndex.PLAYER.POSE, nmsPlayer.getPose());
                    this.remove(ValueIndex.PLAYER.POSE);
                }
            }
        }
    }
}