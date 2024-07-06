package xiamomc.morph.backends.server.renderer.network.datawatcher.watchers.types;

import com.destroystokyo.paper.ClientOption;
import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerDestroyEntities;
import com.mojang.authlib.GameProfile;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import xiamomc.morph.backends.server.renderer.network.DisplayParameters;
import xiamomc.morph.backends.server.renderer.network.registries.EntryIndex;
import xiamomc.morph.backends.server.renderer.network.registries.RegistryKey;
import xiamomc.morph.backends.server.renderer.network.registries.ValueIndex;

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
        this.write(ValueIndex.PLAYER.SKIN_FLAGS, (byte)getBindingPlayer().getClientOption(ClientOption.SKIN_PARTS).getRaw());
        this.write(ValueIndex.PLAYER.MAINHAND, (byte)getBindingPlayer().getMainHand().ordinal());

        super.doSync();
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

            var packetRemove = new WrapperPlayServerDestroyEntities(player.getEntityId());
            var protocol = PacketEvents.getAPI().getPlayerManager();

            var affected = getAffectedPlayers(player);
            affected.forEach(p ->
            {
                protocol.sendPacket(p, packetRemove);

                spawnPackets.forEach(packet -> protocol.sendPacket(p, packet));
            });
        }
    }
}
