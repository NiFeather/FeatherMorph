package xiamomc.morph.backends.server.renderer.network.listeners;

import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityEquipment;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.bukkit.entity.Player;
import xiamomc.morph.backends.server.renderer.network.registries.EntryIndex;
import xiamomc.morph.backends.server.renderer.network.registries.RenderRegistry;
import xiamomc.morph.backends.server.renderer.utilties.PacketUtils;
import xiamomc.pluginbase.Annotations.Resolved;

import java.util.Map;

public class EquipmentPacketListener extends ProtocolListener
{
    @Resolved(shouldSolveImmediately = true)
    private RenderRegistry registry;

    public EquipmentPacketListener()
    {
        registry.onUnRegister(this, alreadyFake::remove);
    }

    @Override
    public String getIdentifier()
    {
        return "equip_listener";
    }

    public void onPacketSending(PacketSendEvent event)
    {
        if (event.getPacketType() != PacketType.Play.Server.ENTITY_EQUIPMENT)
            return;

        var packet = new WrapperPlayServerEntityEquipment(event);

        if (PacketUtils.isPacketOurs(packet))
        {
            PacketUtils.removeMark(packet);
            return;
        }

        onEquipmentPacket(packet, event);
    }

    private final Map<Player, Boolean> alreadyFake = new Object2ObjectOpenHashMap<>();

    private void onEquipmentPacket(WrapperPlayServerEntityEquipment packet, PacketSendEvent event)
    {
        if (PacketUtils.isPacketOurs(packet))
            return;

        //获取此包的来源实体
        var sourceNmsEntity = getNmsPlayerEntityFrom(packet.getEntityId());
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

        packet.setEquipment(getFactory().getPacketEquipmentList(sourcePlayer, watcher));

        alreadyFake.put(sourcePlayer, true);
    }
}
