package xiamomc.morph.backends.server.renderer.network.listeners;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedDataValue;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.bukkit.entity.Player;
import xiamomc.morph.MorphPluginObject;
import xiamomc.morph.backends.server.renderer.network.RegistryParameters;
import xiamomc.morph.backends.server.renderer.network.datawatcher.watchers.SingleWatcher;
import xiamomc.morph.backends.server.renderer.utilties.ProtocolRegistryUtils;

import java.util.List;
import java.util.UUID;

public abstract class ProtocolListener extends MorphPluginObject
{
    protected PacketContainer getMetaPackets(Player player, SingleWatcher watcher)
    {
        var metaPacket = new PacketContainer(PacketType.Play.Server.ENTITY_METADATA);
        metaPacket.getIntegers().write(0, player.getEntityId());

        var modifier = metaPacket.getDataValueCollectionModifier();

        var entityWatcher = WrappedDataWatcher.getEntityWatcher(player);
        entityWatcher.asMap().forEach((id, val) ->
        {
            //logger.info("Id '%s' is val '%s' raw '%s' class '%s'"
            //        .formatted(id, val.getValue(),val.getRawValue(),val.getRawValue().getClass()));
        });

        List<WrappedDataValue> wrappedDataValues = new ObjectArrayList<>();

        watcher.doSync();
        watcher.getRegistry().forEach((single, v) ->
        {
            if (single.defaultValue().equals(v)) return;

            WrappedDataWatcher.Serializer serializer;

            try
            {
                serializer = ProtocolRegistryUtils.getSerializer(single.defaultValue());
            }
            catch (Throwable t)
            {
                logger.warn("Error occurred while generating meta packet with id '%s': %s".formatted(single.index(), t.getMessage()));
                return;
            }

            var value = new WrappedDataValue(single.index(), serializer, v);
            wrappedDataValues.add(value);
            //logger.info("Writing value '%s' index '%s'".formatted(v, single.index()));
        });

        modifier.write(0, wrappedDataValues);
        metaPacket.setMeta("fm", true);

        return metaPacket;
    }

    protected ProtocolManager protocolManager()
    {
        return ProtocolLibrary.getProtocolManager();
    }
}
