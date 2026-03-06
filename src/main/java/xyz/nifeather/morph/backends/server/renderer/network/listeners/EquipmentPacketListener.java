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
 * todo: This might catch packets sent by us? We probably don't want this to happen, as it will cause unnecessary performance cost.
 */
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
    public void onPacketSend(PacketSendEvent event)
    {
        if (event.getPacketType() != PacketType.Play.Server.ENTITY_EQUIPMENT)
            return;

        var wrapper = new WrapperPlayServerEntityEquipment(event);

        onEquipmentPacket(wrapper, event);
    }

    private final Map<Player, Boolean> alreadyFake = new Object2ObjectOpenHashMap<>();

    private void onEquipmentPacket(WrapperPlayServerEntityEquipment packet, PacketSendEvent event)
    {
        if (PacketFactory.isEquipmentPacketOurs(packet))
        {
            packet.setEntityId(Math.abs(packet.getEntityId()));
            return;
        }

        //获取此包的来源实体
        var sourcePlayer = getPlayerFrom(packet.getEntityId());
        if (sourcePlayer == null)
            return;

        var watcher = registry.getWatcher(sourcePlayer.getUniqueId());

        if (watcher == null)
            return;

        if (!watcher.readEntryOrDefault(CustomEntries.DISPLAY_FAKE_EQUIPMENT, false))
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

        event.markForReEncode(true);
        var equipments = PacketFactory.getPacketeventsEquipments(sourcePlayer, watcher);
        packet.setEquipment(equipments);

        alreadyFake.put(sourcePlayer, true);
    }
}
