package xyz.nifeather.morph.backends.server.renderer.network;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedDataValue;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.network.protocol.game.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import xyz.nifeather.morph.MorphPluginObject;
import xyz.nifeather.morph.backends.server.renderer.network.datawatcher.values.SingleValue;
import xyz.nifeather.morph.backends.server.renderer.network.datawatcher.watchers.SingleWatcher;
import xyz.nifeather.morph.backends.server.renderer.network.registries.CustomEntries;
import xyz.nifeather.morph.misc.DisguiseEquipment;

import java.util.List;

/**
 * This is going to be removed!
 */
public class PacketFactory extends MorphPluginObject
{
    public static PacketContainer buildDiffMetaPacket(SingleWatcher watcher)
    {
        List<EntityData> wrappedDataValues = new ObjectArrayList<>();
        var valuesToSent = watcher.getDirty();
        watcher.clearDirty();

        // Add our packet identifier!
        if (!watcher.readEntryOrDefault(CustomEntries.DONT_INCLUDE_PACKET_IDENTIFIER, false))
            wrappedDataValues.add(new WrappedDataValue(99, ProtocolRegistryUtils.getSerializer(MARK_DONT_PROCESS), MARK_DONT_PROCESS));

        valuesToSent.forEach((single, val) ->
        {
            var wrapped =  new EntityData(single.index(), single.type(), val);
            wrappedDataValues.add(wrapped);
        });

        modifier.write(0, wrappedDataValues);

        return metaPacket;
    }

    public static final String MARK_DONT_PROCESS = "~FEATHERMORPH GENERATED METADATA, THIS MESSAGE SHOULD BE REMOVED, OR SOMETHING MAY GONE WRONG!";

    public static PacketContainer buildFullMetaPacket(Player player, SingleWatcher watcher)
    {
        watcher.sync();

        List<EntityData> wrappedDataValues = new ObjectArrayList<>();

        // Add our packet identifier!
        if (!watcher.readEntryOrDefault(CustomEntries.DONT_INCLUDE_PACKET_IDENTIFIER, false))
            wrappedDataValues.add(new WrappedDataValue(99, ProtocolRegistryUtils.getSerializer(MARK_DONT_PROCESS), MARK_DONT_PROCESS));

        var valuesToSent = watcher.getOverlayedRegistry();
        watcher.clearDirty();

        valuesToSent.forEach((index, val) ->
        {
            var sv = watcher.getSingle(index);

            if (sv == null)
                throw new IllegalArgumentException("Not SingleValue found for index " + index);

            var wrapped =  new EntityData(index, sv.type(), val);
            wrappedDataValues.add(wrapped);
        });

        modifier.write(0, wrappedDataValues);

        return metaPacket;
    }

    public static PacketContainer getEquipmentPacket(Player player, SingleWatcher watcher)
    {
        var shouldDisplayFakeEquip = watcher.readEntryOrDefault(CustomEntries.DISPLAY_FAKE_EQUIPMENT, false);
        EntityEquipment equipment = shouldDisplayFakeEquip
                ? watcher.readEntryOrDefault(CustomEntries.EQUIPMENT, new DisguiseEquipment())
                : player.getEquipment();

        return ProtocolEquipment.toPEEquipmentList(equipment);
    }
}
