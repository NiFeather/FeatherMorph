package xiamomc.morph.backends.server.renderer.network.listeners;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListeningWhitelist;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.injector.GamePhase;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.network.protocol.game.ClientboundSetEquipmentPacket;
import org.bukkit.entity.Player;
import xiamomc.morph.backends.server.renderer.network.PacketFactory;
import xiamomc.morph.backends.server.renderer.network.registries.EntryIndex;
import xiamomc.morph.backends.server.renderer.network.registries.RenderRegistry;
import xiamomc.pluginbase.Annotations.Resolved;

import java.util.Map;

public class EquipmentPacketListener extends ProtocolListener
{
    @Resolved(shouldSolveImmediately = true)
    private RenderRegistry registry;

    public EquipmentPacketListener()
    {
        registry.onUnRegister(this, parameters -> alreadyFake.remove(parameters.player()));
    }

    @Override
    public String getIdentifier()
    {
        return "equip_listener";
    }

    @Override
    public void onPacketSending(PacketEvent event)
    {
        if (event.getPacketType() != PacketType.Play.Server.ENTITY_EQUIPMENT)
            return;

        var packet = event.getPacket();

        //不要处理来自我们自己的包
        if (packet.getMeta(PacketFactory.MORPH_PACKET_METAKEY).isPresent())
            return;

        onEquipmentPacket((ClientboundSetEquipmentPacket) event.getPacket().getHandle(), event);
    }

    private final Map<Player, Boolean> alreadyFake = new Object2ObjectOpenHashMap<>();

    private void onEquipmentPacket(ClientboundSetEquipmentPacket packet, PacketEvent event)
    {
        if (event.getPacket().getMeta(PacketFactory.MORPH_PACKET_METAKEY).isPresent())
            return;

        //获取此包的来源实体
        var sourceNmsEntity = getNmsPlayerEntityFrom(event, packet.getEntity());
        if (sourceNmsEntity == null)
            return;

        if (!(sourceNmsEntity.getBukkitEntity() instanceof Player sourcePlayer)) return;

        var watcher = registry.getWatcher(sourcePlayer.getUniqueId());

        if (watcher == null)
            return;

        if (!watcher.getOrDefault(EntryIndex.DISPLAY_FAKE_EQUIPMENT, false))
        {
            alreadyFake.remove(sourcePlayer);
            return;
        }

        if (alreadyFake.getOrDefault(sourcePlayer, false))
        {
            //如果已经在显示伪装物品，那么只取消此包
            event.setCancelled(true);
            return;
        }

        event.setPacket(getFactory().getEquipmentPacket(sourcePlayer, watcher));

        alreadyFake.put(sourcePlayer, true);
    }

    @Override
    public void onPacketReceiving(PacketEvent event)
    {
    }

    @Override
    public ListeningWhitelist getSendingWhitelist()
    {
        return ListeningWhitelist
                .newBuilder()
                .types(PacketType.Play.Server.ENTITY_EQUIPMENT)
                .gamePhase(GamePhase.PLAYING)
                .build();
    }

    @Override
    public ListeningWhitelist getReceivingWhitelist()
    {
        return ListeningWhitelist.EMPTY_WHITELIST;
    }
}
