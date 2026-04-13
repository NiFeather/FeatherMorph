package xyz.nifeather.morph.backends.server.renderer.network;

import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import com.github.retrooper.packetevents.protocol.player.Equipment;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityEquipment;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityMetadata;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import xyz.nifeather.morph.FeatherMorphMain;
import xyz.nifeather.morph.MorphPluginObject;
import xyz.nifeather.morph.backends.server.renderer.network.datawatcher.watchers.BindableVirtualEntity;
import xyz.nifeather.morph.backends.server.renderer.network.datawatcher.watchers.VirtualEntity;
import xyz.nifeather.morph.backends.server.renderer.network.registries.CustomEntries;
import xyz.nifeather.morph.backends.server.renderer.network.registries.ValueIndex;
import xyz.nifeather.morph.misc.DisguiseEquipment;

import java.util.List;

/**
 * This is going to be removed!
 */
public class PacketFactory extends MorphPluginObject
{
    public static WrapperPlayServerEntityMetadata buildDiffMetaPacket(VirtualEntity watcher)
    {
        List<EntityData<?>> wrappedDataValues = new ObjectArrayList<>();
        var valuesToSent = watcher.getDirty();
        watcher.clearDirty();

        // Add our packet identifier!
        if (!watcher.readEntryOrDefault(CustomEntries.DONT_INCLUDE_PACKET_IDENTIFIER, false))
            wrappedDataValues.add(new EntityData<>(99, EntityDataTypes.STRING, MARK_DONT_PROCESS));

        valuesToSent.forEach((single, val) ->
        {
            var wrapped =  new EntityData(single.index(), single.type(), val);
            wrappedDataValues.add(wrapped);
        });

        return new WrapperPlayServerEntityMetadata(watcher.readEntryOrThrow(CustomEntries.SPAWN_ID), wrappedDataValues);
    }

    public static final String MARK_DONT_PROCESS = "~FEATHERMORPH GENERATED METADATA, THIS MESSAGE SHOULD BE REMOVED, OR SOMETHING MAY GONE WRONG!";

    public static List<Equipment> getPacketeventsEquipments(LivingEntity entity, VirtualEntity watcher)
    {
        var shouldDisplayFakeEquip = watcher.readEntryOrDefault(CustomEntries.DISPLAY_FAKE_EQUIPMENT, false);
        DisguiseEquipment equipment = shouldDisplayFakeEquip
                ? watcher.readEntryOrDefault(CustomEntries.EQUIPMENT, DisguiseEquipment.empty())
                : DisguiseEquipment.copy(entity.getEquipment());

        return ProtocolEquipment.toPEEquipmentList(equipment);
    }

    public static void markEquipmentPacket(WrapperPlayServerEntityEquipment wrapper)
    {
        wrapper.setEntityId(-wrapper.getEntityId());
    }

    public static boolean isEquipmentPacketOurs(World world, WrapperPlayServerEntityEquipment wrapper)
    {
        if (wrapper.getEntityId() > 0)
            return false;

        var playerFound = SpigotConversionUtil.getEntityById(world, Math.abs(wrapper.getEntityId()));

        return playerFound != null;
    }
}
