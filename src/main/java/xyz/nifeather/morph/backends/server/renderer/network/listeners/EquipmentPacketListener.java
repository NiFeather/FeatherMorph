package xyz.nifeather.morph.backends.server.renderer.network.listeners;

import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityEquipment;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.bukkit.entity.Player;
import xiamomc.pluginbase.Annotations.Resolved;
import xyz.nifeather.morph.backends.server.renderer.network.PacketFactory;
import xyz.nifeather.morph.backends.server.renderer.network.registries.CustomEntries;
import xyz.nifeather.morph.backends.server.renderer.network.registries.RenderRegistry;

import java.util.Map;

/**
 * Listener used to override the equipment so that we can display the disguise's equipment.
 */
public class EquipmentPacketListener extends ProtocolListener
{
    @Resolved(shouldSolveImmediately = true)
    private RenderRegistry registry;

    public EquipmentPacketListener()
    {
    }

    @Override
    public String getIdentifier()
    {
        return "equip_listener";
    }

    @Override
    public void onPacketSend(PacketSendEvent event)
    {
        if (event.getPacketType() != PacketType.Play.Server.ENTITY_EQUIPMENT)
            return;

        var wrapper = new WrapperPlayServerEntityEquipment(event);

        onEquipmentPacket(wrapper, event);
    }

    private void onEquipmentPacket(WrapperPlayServerEntityEquipment packet, PacketSendEvent event)
    {
        //获取此包的来源实体
        var sourcePlayer = getPlayerFrom(packet.getEntityId());
        if (sourcePlayer == null)
            return;

        var watcher = registry.getWatcher(sourcePlayer.getUniqueId());

        if (watcher == null)
            return;

        if (watcher.readEntryOrDefault(CustomEntries.DISPLAY_FAKE_EQUIPMENT, false))
            event.setCancelled(true);
    }
}
