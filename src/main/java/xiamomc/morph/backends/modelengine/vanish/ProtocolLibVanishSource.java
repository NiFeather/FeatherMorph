package xiamomc.morph.backends.modelengine.vanish;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerDestroyEntities;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSpawnEntity;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import xiamomc.morph.MorphPluginObject;
import xiamomc.morph.backends.server.renderer.network.PacketFactory;
import xiamomc.morph.backends.server.renderer.network.datawatcher.watchers.types.PlayerWatcher;
import xiamomc.morph.backends.server.renderer.utilties.WatcherUtils;
import xiamomc.morph.misc.NmsRecord;
import xiamomc.morph.utilities.EntityTypeUtils;

import java.util.List;

public class ProtocolLibVanishSource extends MorphPluginObject implements IVanishSource
{
    public static class VanishListenerWrapper extends PacketListenerAbstract
    {
        private final ProtocolLibVanishSource listener;

        public VanishListenerWrapper(ProtocolLibVanishSource protocolListener)
        {
            this.listener = protocolListener;
        }

        @Override
        public void onPacketSend(PacketSendEvent event)
        {
            super.onPacketSend(event);

            this.listener.onPacketSending(event);
        }
    }

    private final VanishListenerWrapper wrapper;

    public ProtocolLibVanishSource()
    {
        this.wrapper = new VanishListenerWrapper(this);
        PacketEvents.getAPI().getEventManager().registerListener(this.wrapper);
    }

    private final List<Player> vanish = new ObjectArrayList<>();

    @Override
    public void vanishPlayer(Player player)
    {
        vanish.add(player);

        var affected = WatcherUtils.getAffectedPlayers(player);
        var packet = new WrapperPlayServerDestroyEntities(player.getEntityId());

        var packets = PacketEvents.getAPI().getPlayerManager();
        for (var affectedPlayer : affected)
            packets.sendPacket(affectedPlayer, packet);
    }

    private final PacketFactory packetFactory = new PacketFactory();

    @Override
    public void cancelVanish(Player player)
    {
        vanish.remove(player);

        var watcher = new PlayerWatcher(player);
        watcher.sync();

        var velocity = player.getVelocity();
        var packet = new WrapperPlayServerSpawnEntity(
                player.getEntityId(), player.getUniqueId(),
                SpigotConversionUtil.fromBukkitEntityType(EntityType.PLAYER),
                SpigotConversionUtil.fromBukkitLocation(player.getLocation()),
                player.getYaw(), 0, new Vector3d(velocity.getX(), velocity.getY(), velocity.getZ())
        );

        var manager = PacketEvents.getAPI().getPlayerManager();
        for (Player affected : WatcherUtils.getAffectedPlayers(player))
            manager.sendPacket(affected, packet);
    }

    public void onPacketSending(PacketSendEvent packetEvent)
    {
        if (packetEvent.getPacketType() != PacketType.Play.Server.SPAWN_ENTITY) return;

        var packet = new WrapperPlayServerSpawnEntity(packetEvent);
        if (vanish.stream().anyMatch(p -> packet.getEntityId() == p.getEntityId()))
            packetEvent.setCancelled(true);
    }
}
