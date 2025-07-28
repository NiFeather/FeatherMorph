package xyz.nifeather.morph.backends.server.renderer.network;

import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import com.github.retrooper.packetevents.protocol.player.Equipment;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityEquipment;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityMetadata;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import xyz.nifeather.morph.FeatherMorphMain;
import xyz.nifeather.morph.MorphPluginObject;
import xyz.nifeather.morph.backends.server.renderer.network.datawatcher.watchers.SingleWatcher;
import xyz.nifeather.morph.backends.server.renderer.network.registries.CustomEntries;
import xyz.nifeather.morph.misc.DisguiseEquipment;

import java.util.List;

/**
 * This is going to be removed!
 */
public class PacketFactory extends MorphPluginObject
{
    public static WrapperPlayServerEntityMetadata buildDiffMetaPacket(SingleWatcher watcher)
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

        return new WrapperPlayServerEntityMetadata(watcher.getBindingPlayer().getEntityId(), wrappedDataValues);
    }

    public static final String MARK_DONT_PROCESS = "~FEATHERMORPH GENERATED METADATA, THIS MESSAGE SHOULD BE REMOVED, OR SOMETHING MAY GONE WRONG!";

    public static WrapperPlayServerEntityMetadata buildFullMetaPacket(Player player, SingleWatcher watcher)
    {
        watcher.sync();

        List<EntityData<?>> wrappedDataValues = new ObjectArrayList<>();

        // Add our packet identifier!
        if (!watcher.readEntryOrDefault(CustomEntries.DONT_INCLUDE_PACKET_IDENTIFIER, false))
            wrappedDataValues.add(new EntityData<>(99, EntityDataTypes.STRING, MARK_DONT_PROCESS));

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

        return new WrapperPlayServerEntityMetadata(player.getEntityId(), wrappedDataValues);
    }

    public static List<Equipment> getPacketeventsEquipments(Player player, SingleWatcher watcher)
    {
        var shouldDisplayFakeEquip = watcher.readEntryOrDefault(CustomEntries.DISPLAY_FAKE_EQUIPMENT, false);
        EntityEquipment equipment = shouldDisplayFakeEquip
                ? watcher.readEntryOrDefault(CustomEntries.EQUIPMENT, new DisguiseEquipment())
                : player.getEquipment();

        return ProtocolEquipment.toPEEquipmentList(equipment);
    }

    public static void markEquipmentPacket(WrapperPlayServerEntityEquipment wrapper)
    {
        wrapper.setEntityId(-wrapper.getEntityId());
    }

    public static boolean isEquipmentPacketOurs(WrapperPlayServerEntityEquipment wrapper)
    {
        if (wrapper.getEntityId() > 0)
            return false;

        var abs = Math.abs(wrapper.getEntityId());

        var playerFound = FeatherMorphMain.getInstance()
                .getPlatform()
                .onlinePlayers()
                .stream()
                .filter(p -> p.getEntityId() == abs)
                .findFirst()
                .orElse(null);

        return playerFound != null;
    }
}
